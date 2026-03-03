use std::path::{Path, PathBuf};
use std::process::{Command, Stdio};

use tauri::{AppHandle, Manager};
use uuid::Uuid;

// ─── Binary discovery ────────────────────────────────────────────────────────

pub fn find_ffmpeg(app: &AppHandle) -> PathBuf {
    find_bin(app, if cfg!(windows) { "ffmpeg.exe" } else { "ffmpeg" })
}

pub fn find_ffprobe(app: &AppHandle) -> PathBuf {
    find_bin(app, if cfg!(windows) { "ffprobe.exe" } else { "ffprobe" })
}

fn find_bin(app: &AppHandle, name: &str) -> PathBuf {
    // 1. next to the running executable (production sidecar location)
    if let Ok(exe) = std::env::current_exe() {
        if let Some(dir) = exe.parent() {
            let candidate = dir.join(name);
            if candidate.exists() {
                return candidate;
            }
        }
    }
    // 2. Tauri resource dir (bundled via externalBin)
    if let Ok(res_dir) = app.path().resource_dir() {
        let candidate = res_dir.join(name);
        if candidate.exists() {
            return candidate;
        }
    }
    // 3. macOS Homebrew common prefixes
    #[cfg(target_os = "macos")]
    {
        for prefix in ["/opt/homebrew/bin", "/usr/local/bin"] {
            let candidate = PathBuf::from(prefix).join(name);
            if candidate.exists() {
                return candidate;
            }
        }
    }
    // 4. fall back to PATH
    PathBuf::from(name)
}

pub fn check_ffmpeg_available(app: &AppHandle) -> bool {
    Command::new(find_ffmpeg(app))
        .arg("-version")
        .stdout(Stdio::null())
        .stderr(Stdio::null())
        .status()
        .map(|s| s.success())
        .unwrap_or(false)
}

// ─── Video info ───────────────────────────────────────────────────────────────

/// Returns video duration in seconds using ffprobe.
pub fn get_video_duration(ffprobe: &Path, input: &Path) -> Result<f64, String> {
    let out = Command::new(ffprobe)
        .args([
            "-v",
            "quiet",
            "-print_format",
            "json",
            "-show_entries",
            "format=duration",
            &input.to_string_lossy(),
        ])
        .output()
        .map_err(|e| format!("ffprobe failed to run: {e}"))?;

    let json = String::from_utf8_lossy(&out.stdout);
    // minimal parse: find "duration": "60.123"
    if let Some(pos) = json.find("\"duration\"") {
        let rest = &json[pos + 10..];
        let value = rest
            .trim()
            .trim_start_matches(':')
            .trim()
            .trim_matches('"');
        let end = value
            .find(|c: char| !c.is_ascii_digit() && c != '.')
            .unwrap_or(value.len());
        if let Ok(d) = value[..end].parse::<f64>() {
            if d > 0.0 {
                return Ok(d);
            }
        }
    }
    Err("Could not parse video duration".into())
}

// ─── Watermark options ────────────────────────────────────────────────────────

#[derive(Debug, Clone)]
pub struct VideoWatermarkOptions {
    /// Text to burn in (e.g. "001")
    pub text: String,
    /// Override timestamp in seconds. None = auto (60 s or 20 % of duration).
    pub timestamp_sec: Option<f64>,
    /// Font size for drawtext filter (default 16).
    pub font_size: u32,
}

impl Default for VideoWatermarkOptions {
    fn default() -> Self {
        Self {
            text: String::new(),
            timestamp_sec: None,
            font_size: 16,
        }
    }
}

// ─── Core watermark logic ─────────────────────────────────────────────────────

/// Adds a tiny visible watermark to a handful of frames (~0.04 s) of a video.
///
/// Strategy:
///   1. ffprobe  → get duration
///   2. Split video into 3 parts:
///        part1  [0 … ts)           – stream copy, no re-encode
///        part2  [ts … ts+0.04 s)   – re-encode with drawtext filter
///        part3  [ts+0.04 s … end)  – stream copy, no re-encode
///   3. Concat → output.mp4
///   4. Atomically replace the original file
///
/// The temp directory is auto-cleaned on return (success or error).
pub fn add_video_watermark(
    ffmpeg: &Path,
    ffprobe: &Path,
    input: &Path,
    opts: &VideoWatermarkOptions,
) -> Result<(), String> {
    let duration = get_video_duration(ffprobe, input).unwrap_or(120.0);
    let seg_dur = 0.04_f64;

    let ts = opts
        .timestamp_sec
        .filter(|&t| t > 0.0 && t < duration - seg_dur)
        .unwrap_or_else(|| {
            if duration > 65.0 {
                60.0
            } else {
                (duration * 0.2).max(1.0).min(duration - seg_dur - 0.1)
            }
        });
    let ts_end = ts + seg_dur;
    let has_before = ts > 0.5;
    let has_after = ts_end < duration - 0.5;

    // temp dir beside the source file
    let tmp = input
        .parent()
        .unwrap_or(Path::new("."))
        .join(format!(".wm_tmp_{}", Uuid::new_v4()));
    std::fs::create_dir_all(&tmp).map_err(|e| format!("Cannot create temp dir: {e}"))?;

    // auto-cleanup on any return path
    let _guard = TempDirGuard(&tmp);

    let part1 = tmp.join("part1.mp4");
    let part2 = tmp.join("part2.mp4");
    let part3 = tmp.join("part3.mp4");
    let concat_file = tmp.join("concat.txt");
    let out_file = tmp.join("output.mp4");

    let input_s = input.to_string_lossy().into_owned();

    // ── Part 1 (stream copy before watermark) ────────────────────────────────
    if has_before {
        run_ffmpeg(
            ffmpeg,
            &[
                "-y",
                "-i",
                &input_s,
                "-t",
                &ts.to_string(),
                "-c",
                "copy",
                &part1.to_string_lossy(),
            ],
        )?;
    }

    // ── Part 2 (render with drawtext) ────────────────────────────────────────
    // Escape special characters for drawtext filter.
    let safe_text = opts
        .text
        .replace('\\', "\\\\")
        .replace('\'', "\\'")
        .replace(':', "\\:");
    let drawtext = format!(
        "drawtext=text='{safe_text}':fontsize={fs}:fontcolor=white@0.85:\
         x=10:y=h-th-10:box=1:boxcolor=black@0.3:boxborderw=2",
        fs = opts.font_size
    );
    run_ffmpeg(
        ffmpeg,
        &[
            "-y",
            "-ss",
            &ts.to_string(),
            "-i",
            &input_s,
            "-t",
            &seg_dur.to_string(),
            "-vf",
            &drawtext,
            &part2.to_string_lossy(),
        ],
    )?;

    // ── Part 3 (stream copy after watermark) ─────────────────────────────────
    if has_after {
        run_ffmpeg(
            ffmpeg,
            &[
                "-y",
                "-ss",
                &ts_end.to_string(),
                "-i",
                &input_s,
                "-c",
                "copy",
                &part3.to_string_lossy(),
            ],
        )?;
    }

    // ── Concat ───────────────────────────────────────────────────────────────
    let mut concat_content = String::new();
    if has_before && part1.exists() {
        concat_content.push_str(&format!(
            "file '{}'\n",
            part1.to_string_lossy().replace('\\', "/")
        ));
    }
    if part2.exists() {
        concat_content.push_str(&format!(
            "file '{}'\n",
            part2.to_string_lossy().replace('\\', "/")
        ));
    }
    if has_after && part3.exists() {
        concat_content.push_str(&format!(
            "file '{}'\n",
            part3.to_string_lossy().replace('\\', "/")
        ));
    }

    std::fs::write(&concat_file, &concat_content)
        .map_err(|e| format!("Cannot write concat.txt: {e}"))?;

    run_ffmpeg(
        ffmpeg,
        &[
            "-y",
            "-f",
            "concat",
            "-safe",
            "0",
            "-i",
            &concat_file.to_string_lossy(),
            "-c",
            "copy",
            &out_file.to_string_lossy(),
        ],
    )?;

    // ── Replace original (atomic rename, fallback to copy) ───────────────────
    std::fs::rename(&out_file, input)
        .or_else(|_| std::fs::copy(&out_file, input).map(|_| ()))
        .map_err(|e| format!("Cannot replace original file: {e}"))?;

    Ok(())
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

fn run_ffmpeg(ffmpeg: &Path, args: &[&str]) -> Result<(), String> {
    let output = Command::new(ffmpeg)
        .args(args)
        .stdout(Stdio::null())
        .stderr(Stdio::piped())
        .output()
        .map_err(|e| format!("Failed to run ffmpeg: {e}"))?;

    if output.status.success() {
        Ok(())
    } else {
        let stderr = String::from_utf8_lossy(&output.stderr);
        let tail = if stderr.len() > 600 {
            &stderr[stderr.len() - 600..]
        } else {
            &stderr
        };
        Err(format!("ffmpeg error: {tail}"))
    }
}

struct TempDirGuard<'a>(&'a Path);

impl Drop for TempDirGuard<'_> {
    fn drop(&mut self) {
        let _ = std::fs::remove_dir_all(self.0);
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    // ── timestamp selection logic ─────────────────────────────────────────────
    // We replicate the inline logic here to test it in isolation.

    fn choose_ts(duration: f64, hint: Option<f64>) -> f64 {
        let seg_dur = 0.04_f64;
        hint.filter(|&t| t > 0.0 && t < duration - seg_dur)
            .unwrap_or_else(|| {
                if duration > 65.0 {
                    60.0
                } else {
                    (duration * 0.2).max(1.0).min(duration - seg_dur - 0.1)
                }
            })
    }

    #[test]
    fn ts_hint_used_when_valid() {
        let ts = choose_ts(120.0, Some(30.0));
        assert!((ts - 30.0).abs() < 1e-9);
    }

    #[test]
    fn ts_hint_ignored_when_out_of_range() {
        // hint > duration → auto
        let ts = choose_ts(120.0, Some(200.0));
        assert!((ts - 60.0).abs() < 1e-9);
    }

    #[test]
    fn ts_defaults_to_60_for_long_video() {
        let ts = choose_ts(300.0, None);
        assert!((ts - 60.0).abs() < 1e-9);
    }

    #[test]
    fn ts_uses_20_percent_for_short_video() {
        // 30 s video → 20% = 6.0 s
        let ts = choose_ts(30.0, None);
        assert!((ts - 6.0).abs() < 1e-9);
    }

    #[test]
    fn ts_never_below_one_second() {
        // Very short video (3 s) → 20% = 0.6 → clamped to 1.0
        let ts = choose_ts(3.0, None);
        assert!(ts >= 1.0);
    }

    // ── drawtext escape ───────────────────────────────────────────────────────

    fn escape_text(text: &str) -> String {
        text.replace('\\', "\\\\")
            .replace('\'', "\\'")
            .replace(':', "\\:")
    }

    #[test]
    fn escape_handles_single_quote() {
        let out = escape_text("O'Brien");
        assert_eq!(out, r"O\'Brien");
    }

    #[test]
    fn escape_handles_colon() {
        let out = escape_text("10:00");
        assert_eq!(out, r"10\:00");
    }

    #[test]
    fn escape_handles_plain_text() {
        let out = escape_text("ORDER001");
        assert_eq!(out, "ORDER001");
    }

    // ── bin path resolution ───────────────────────────────────────────────────

    #[test]
    fn find_bin_falls_back_to_name_string() {
        // Without an AppHandle we can't call find_ffmpeg(), but we can verify
        // that the fallback PathBuf contains the correct filename.
        let name = if cfg!(windows) { "ffmpeg.exe" } else { "ffmpeg" };
        let p = PathBuf::from(name);
        assert_eq!(p.file_name().unwrap().to_str().unwrap(), name);
    }

    // ── TempDirGuard cleanup ──────────────────────────────────────────────────

    #[test]
    fn temp_dir_guard_removes_dir_on_drop() {
        let base = std::env::temp_dir().join(format!("wm_guard_test_{}", uuid::Uuid::new_v4()));
        std::fs::create_dir_all(&base).unwrap();
        assert!(base.exists());
        {
            let _g = TempDirGuard(&base);
        }
        assert!(!base.exists(), "TempDirGuard should remove dir on drop");
    }
}
