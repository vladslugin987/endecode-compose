use std::{
    fs::{self, File},
    io::{Read, Write},
    path::{Path, PathBuf},
};

use regex::Regex;
use zip::{write::SimpleFileOptions, CompressionMethod, ZipWriter};

use super::{
    encoding, fs_utils,
    image_text::{TextPosition, VisibleWatermarkSize, VisibleWatermarkStyle},
    video_watermark::{self, VideoWatermarkOptions},
    watermark,
};

#[derive(Debug, Clone)]
pub struct BatchOptions {
    pub source_folder: PathBuf,
    pub num_copies: usize,
    pub base_text: String,
    pub add_swap: bool,
    pub add_watermark: bool,
    pub create_zip: bool,
    pub watermark_text: Option<String>,
    pub photo_number: Option<u32>,
    pub visible_size: Option<String>,
    pub visible_opacity: Option<u8>,
    /// Direct pixel scale (1–24) for the visible photo watermark. Overrides visible_size.
    pub visible_scale: Option<u32>,
    /// Whether to also burn a tiny visible watermark into video files using FFmpeg.
    pub add_video_watermark: bool,
    pub video_watermark_text: Option<String>,
    pub video_watermark_timestamp_sec: Option<f64>,
    pub video_watermark_font_size: Option<u32>,
    /// Paths to ffmpeg/ffprobe binaries (required when add_video_watermark is true).
    pub ffmpeg_path: Option<PathBuf>,
    pub ffprobe_path: Option<PathBuf>,
}

pub fn perform_batch_copy_and_encode<F, C>(
    options: BatchOptions,
    mut on_progress: F,
    mut on_log: impl FnMut(&str, String),
    mut is_cancelled: C,
) -> Result<PathBuf, String>
where
    F: FnMut(f32, Option<String>, Option<String>),
    C: FnMut() -> bool,
{
    let source_folder = options.source_folder;
    let copies_folder = source_folder
        .parent()
        .unwrap_or_else(|| Path::new("."))
        .join(format!(
            "{}-Copies",
            source_folder.file_name().and_then(|n| n.to_str()).unwrap_or("Source")
        ));
    if !copies_folder.exists() {
        fs::create_dir_all(&copies_folder)
            .map_err(|e| format!("Error creating copies folder {}: {e}", copies_folder.display()))?;
        on_log("info", format!("Created copies folder: {}", copies_folder.display()));
    }

    let start_number = extract_start_number(&options.base_text);
    let base_text_without_number = Regex::new(r"\d+$")
        .map_err(|e| format!("Regex error: {e}"))?
        .replace(options.base_text.trim(), "")
        .to_string()
        .trim()
        .to_string();

    let mut total_ops = options.num_copies as f32 * 2.0;
    if options.add_watermark {
        total_ops += options.num_copies as f32;
    }
    if options.add_swap {
        total_ops += options.num_copies as f32;
    }
    if options.add_video_watermark {
        total_ops += options.num_copies as f32;
    }
    if options.create_zip {
        total_ops += options.num_copies as f32;
    }
    let mut completed = 0.0_f32;
    let mut folders_to_zip: Vec<PathBuf> = Vec::new();

    for i in 0..options.num_copies {
        if is_cancelled() {
            return Err("Cancelled".to_string());
        }
        let order_number = format!("{:03}", start_number + i as u32);
        let order_folder = copies_folder.join(&order_number);
        fs::create_dir_all(&order_folder)
            .map_err(|e| format!("Error creating order folder {}: {e}", order_folder.display()))?;
        on_log("info", format!("Processing order folder: {order_number}"));
        let destination = order_folder.join(
            source_folder
                .file_name()
                .and_then(|v| v.to_str())
                .unwrap_or("Bundle"),
        );

        fs_utils::copy_directory(&source_folder, &destination)?;
        on_log("success", format!("Copied source to {}", destination.display()));
        completed += 1.0;
        on_progress(completed / total_ops, None, Some("copy".to_string()));

        let style = VisibleWatermarkStyle {
            size: VisibleWatermarkSize::from_optional_str(options.visible_size.as_deref()),
            opacity: options.visible_opacity.unwrap_or(200).clamp(30, 255),
            scale_override: options.visible_scale.map(|s| s.clamp(1, 24)),
        };
        process_files(&destination, &base_text_without_number, &order_number, &mut on_log)?;
        completed += 1.0;
        on_progress(completed / total_ops, None, Some("encode".to_string()));

        if options.add_watermark {
            let actual_photo_number = options.photo_number.unwrap_or(order_number.parse().unwrap_or(1));
            let actual_text = options
                .watermark_text
                .clone()
                .unwrap_or_else(|| order_number.clone());
            add_visible_watermark_to_photo(&destination, &actual_text, actual_photo_number, style, &mut on_log)?;
            completed += 1.0;
            on_progress(completed / total_ops, None, Some("watermark".to_string()));
        }

        if options.add_swap {
            perform_swap(&destination, &order_number, &mut on_log)?;
            completed += 1.0;
            on_progress(completed / total_ops, None, Some("swap".to_string()));
        }

        if options.add_video_watermark {
            if let Some(ffmpeg) = &options.ffmpeg_path {
                let ffprobe = options.ffprobe_path.as_deref().unwrap_or(ffmpeg.as_path());
                let vw_text = options
                    .video_watermark_text
                    .clone()
                    .unwrap_or_else(|| order_number.clone());
                let vw_opts = VideoWatermarkOptions {
                    text: vw_text.clone(),
                    timestamp_sec: options.video_watermark_timestamp_sec,
                    font_size: options.video_watermark_font_size.unwrap_or(12),
                };
                for video in fs_utils::get_supported_files(&destination)
                    .into_iter()
                    .filter(|p| fs_utils::is_video_file(p))
                {
                    match video_watermark::add_video_watermark(ffmpeg, ffprobe, &video, &vw_opts) {
                        Ok(_) => on_log("success", format!("Video watermark added to {}", video.display())),
                        Err(e) => on_log("warn", format!("Video watermark failed for {}: {e}", video.display())),
                    }
                }
            } else {
                on_log("warn", format!("Skipping video watermark for {order_number}: FFmpeg not found"));
            }
            completed += 1.0;
            on_progress(completed / total_ops, None, Some("video_wm".to_string()));
        }

        folders_to_zip.push(destination);
    }

    if options.create_zip {
        for folder in &folders_to_zip {
            if is_cancelled() {
                return Err("Cancelled".to_string());
            }
            create_no_compression_zip(folder)?;
            on_log("success", format!("Created ZIP for {}", folder.display()));
            fs::remove_dir_all(folder)
                .map_err(|e| format!("Error removing folder {}: {e}", folder.display()))?;
            completed += 1.0;
            on_progress(completed / total_ops, None, Some("zip".to_string()));
        }
    }

    Ok(copies_folder)
}

fn process_files(
    folder: &Path,
    base_text: &str,
    order_number: &str,
    on_log: &mut impl FnMut(&str, String),
) -> Result<(), String> {
    let files = fs_utils::get_supported_files(folder);
    let encoded_text = format!("{base_text} {order_number}");
    let encoded_watermark = encoding::encode_text(&encoded_text);
    let watermark_text = encoding::add_watermark(&encoded_text);

    for file in files {
        if file
            .extension()
            .and_then(|ext| ext.to_str())
            .map(|ext| ext.eq_ignore_ascii_case("txt"))
            .unwrap_or(false)
        {
            let _ = encoding::process_file(&file, &watermark_text)?;
            on_log("info", format!("Encoded text watermark in {}", file.display()));
        } else if fs_utils::is_video_file(&file) || fs_utils::is_image_file(&file) {
            let _ = watermark::add_watermark(&file, &encoded_watermark)?;
            on_log("info", format!("Added invisible watermark to {}", file.display()));
        } else {
            // For other binary-safe supported formats, also use tail watermark.
            let _ = watermark::add_watermark(&file, &encoded_watermark)?;
            on_log("info", format!("Added tail watermark to {}", file.display()));
        }
    }

    Ok(())
}

fn add_visible_watermark_to_photo(
    folder: &Path,
    watermark_text: &str,
    photo_number: u32,
    style: VisibleWatermarkStyle,
    on_log: &mut impl FnMut(&str, String),
) -> Result<(), String> {
    let files = fs_utils::get_supported_files(folder);
    let mut applied = false;
    for file in files.into_iter().filter(|p| fs_utils::is_image_file(p)) {
        if extract_file_number(file.file_name().and_then(|n| n.to_str()).unwrap_or_default())
            == Some(photo_number)
        {
            let _ = super::image_text::add_text_to_image_with_style(
                &file,
                watermark_text,
                TextPosition::BottomRight,
                style,
            )?;
            on_log("success", format!("Added visible watermark to {}", file.display()));
            applied = true;
            break;
        }
    }
    if !applied {
        if let Some(first_image) = fs_utils::get_supported_files(folder)
            .into_iter()
            .find(|p| fs_utils::is_image_file(p))
        {
            let _ = super::image_text::add_text_to_image_with_style(
                &first_image,
                watermark_text,
                TextPosition::BottomRight,
                style,
            )?;
            on_log(
                "warn",
                format!(
                    "Photo number {} not found, fallback visible watermark to {}",
                    photo_number,
                    first_image.display()
                ),
            );
        } else {
            on_log("warn", format!("No images found in {} for visible watermark", folder.display()));
        }
    }
    Ok(())
}

fn perform_swap(
    folder: &Path,
    order_number: &str,
    on_log: &mut impl FnMut(&str, String),
) -> Result<(), String> {
    let base_number: u32 = order_number.parse().unwrap_or(1);
    let swap_number = base_number + 10;
    let all_images: Vec<PathBuf> = fs_utils::get_supported_files(folder)
        .into_iter()
        .filter(|p| fs_utils::is_image_file(p))
        .collect();

    let file_a = all_images
        .iter()
        .find(|p| extract_file_number(p.file_name().and_then(|n| n.to_str()).unwrap_or_default()) == Some(base_number))
        .cloned();
    let file_b = all_images
        .iter()
        .find(|p| extract_file_number(p.file_name().and_then(|n| n.to_str()).unwrap_or_default()) == Some(swap_number))
        .cloned();

    if let (Some(a), Some(b)) = (file_a, file_b) {
        swap_files(&a, &b)?;
        on_log("success", format!("Swapped files: {} <-> {}", a.display(), b.display()));
    } else {
        on_log(
            "warn",
            format!("No swap pair found in {} for order {}", folder.display(), order_number),
        );
    }
    Ok(())
}

fn swap_files(file_a: &Path, file_b: &Path) -> Result<(), String> {
    let temp = file_a.with_file_name(format!(
        "temp_{}_{}",
        std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map(|d| d.as_millis())
            .unwrap_or(0),
        file_a.file_name().and_then(|n| n.to_str()).unwrap_or("a")
    ));
    fs::rename(file_a, &temp).map_err(|e| format!("Error rename A->temp: {e}"))?;
    fs::rename(file_b, file_a).map_err(|e| format!("Error rename B->A: {e}"))?;
    fs::rename(&temp, file_b).map_err(|e| format!("Error rename temp->B: {e}"))?;
    Ok(())
}

/// Returns true for system/metadata files that should NOT be included in the ZIP.
fn should_skip_in_zip(path: &Path) -> bool {
    let Some(name) = path.file_name().and_then(|n| n.to_str()) else {
        return false;
    };
    let lower = name.to_lowercase();
    // Windows metadata, macOS metadata, executables, binary sidecars
    matches!(
        lower.as_str(),
        "desktop.ini"
            | "thumbs.db"
            | ".ds_store"
            | "ffmpeg"
            | "ffmpeg.exe"
            | "ffprobe"
            | "ffprobe.exe"
    ) || lower.starts_with('.')
        || lower.ends_with(".exe")
        || lower.ends_with(".dll")
}

/// Convert a `SystemTime` to a `zip::DateTime` using manual MS-DOS calendar arithmetic.
/// Fixes the "1899" display issue caused by unset ZIP timestamps.
fn zip_dt_from_system_time(t: std::time::SystemTime) -> zip::DateTime {
    let secs = t
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap_or_default()
        .as_secs();

    let (year, month, day, hour, min, sec) = unix_secs_to_civil(secs);

    if (1980..=2107).contains(&year) {
        let dos_date = (((year - 1980) as u16) << 9) | ((month as u16) << 5) | (day as u16);
        let dos_time = ((hour as u16) << 11) | ((min as u16) << 5) | ((sec / 2) as u16);
        if let Ok(dt) = zip::DateTime::try_from((dos_date, dos_time)) {
            return dt;
        }
    }
    // Fallback: current time (guaranteed valid)
    zip::DateTime::default_for_write()
}

/// Decompose a Unix timestamp (seconds since 1970-01-01 UTC) into (year, month, day, hour, min, sec).
/// Uses the Gregorian calendar algorithm via Julian Day Numbers (no external crate needed).
fn unix_secs_to_civil(unix_secs: u64) -> (u32, u32, u32, u32, u32, u32) {
    let time_of_day = unix_secs % 86400;
    let h = (time_of_day / 3600) as u32;
    let m = ((time_of_day % 3600) / 60) as u32;
    let s = (time_of_day % 60) as u32;

    // Julian Day Number: 1970-01-01 = JDN 2440588
    let jdn = (unix_secs / 86400) as i64 + 2_440_588;

    // Gregorian calendar algorithm (Richards 2013)
    let f = jdn + 1401 + (((4 * jdn + 274_277) / 146_097) * 3) / 4 - 38;
    let e = 4 * f + 3;
    let g = (e % 1461) / 4;
    let h2 = 5 * g + 2;
    let day = (h2 % 153) / 5 + 1;
    let month = (h2 / 153 + 2) % 12 + 1;
    let year = e / 1461 - 4716 + (14 - month) / 12;

    (year as u32, month as u32, day as u32, h, m, s)
}

fn create_no_compression_zip(folder_to_zip: &Path) -> Result<(), String> {
    let zip_file = folder_to_zip
        .parent()
        .unwrap_or_else(|| Path::new("."))
        .join(format!(
            "{}.zip",
            folder_to_zip.file_name().and_then(|n| n.to_str()).unwrap_or("bundle")
        ));
    let file = File::create(&zip_file)
        .map_err(|e| format!("Error creating zip {}: {e}", zip_file.display()))?;
    let mut zip = ZipWriter::new(file);
    let base_options = SimpleFileOptions::default().compression_method(CompressionMethod::Stored);

    for entry in walkdir::WalkDir::new(folder_to_zip).into_iter().filter_map(Result::ok) {
        let path = entry.path();

        // Skip system / metadata / binary files
        if path.is_file() && should_skip_in_zip(path) {
            continue;
        }

        let name = path
            .strip_prefix(folder_to_zip)
            .map_err(|e| format!("Path strip error: {e}"))?
            .to_string_lossy()
            .replace('\\', "/");

        if path.is_dir() {
            if !name.is_empty() {
                zip.add_directory(format!("{name}/"), base_options)
                    .map_err(|e| format!("Zip add directory error: {e}"))?;
            }
        } else {
            // Preserve the file's real modification time (fixes "1899" display).
            let last_modified = path
                .metadata()
                .ok()
                .and_then(|m| m.modified().ok())
                .map(zip_dt_from_system_time)
                .unwrap_or_else(|| zip_dt_from_system_time(std::time::SystemTime::now()));

            let options = base_options.last_modified_time(last_modified);
            zip.start_file(name, options)
                .map_err(|e| format!("Zip start file error: {e}"))?;
            let mut f = File::open(path).map_err(|e| format!("Open file error: {e}"))?;
            let mut buffer = Vec::new();
            f.read_to_end(&mut buffer)
                .map_err(|e| format!("Read file error: {e}"))?;
            zip.write_all(&buffer)
                .map_err(|e| format!("Zip write error: {e}"))?;
        }
    }

    zip.finish().map_err(|e| format!("Zip finish error: {e}"))?;
    Ok(())
}

fn extract_file_number(filename: &str) -> Option<u32> {
    let re = Regex::new(r".*?(\d+).*").ok()?;
    re.captures(filename)
        .and_then(|c| c.get(1))
        .and_then(|m| m.as_str().parse::<u32>().ok())
}

#[cfg(test)]
mod tests {
    use super::*;
    use tempfile::TempDir;

    // ── extract_start_number ─────────────────────────────────────────────────

    #[test]
    fn extract_start_number_from_order_string() {
        assert_eq!(extract_start_number("ORDER 007"), 7);
        assert_eq!(extract_start_number("batch 100"), 100);
        assert_eq!(extract_start_number("001"), 1);
    }

    #[test]
    fn extract_start_number_defaults_to_one() {
        assert_eq!(extract_start_number("no numbers here!"), 1);
        assert_eq!(extract_start_number(""), 1);
    }

    #[test]
    fn extract_start_number_uses_trailing_digits() {
        // "ORDER 12" → trailing number is 12
        assert_eq!(extract_start_number("ORDER 12"), 12);
    }

    // ── extract_file_number ──────────────────────────────────────────────────

    #[test]
    fn extract_file_number_from_filename() {
        assert_eq!(extract_file_number("photo_001.jpg"), Some(1));
        assert_eq!(extract_file_number("img42.png"), Some(42));
        assert_eq!(extract_file_number("007_cover.jpg"), Some(7));
    }

    #[test]
    fn extract_file_number_returns_none_for_no_digits() {
        assert_eq!(extract_file_number("cover.jpg"), None);
        assert_eq!(extract_file_number(""), None);
    }

    // ── swap_files ────────────────────────────────────────────────────────────

    #[test]
    fn swap_files_exchanges_content() {
        let dir = TempDir::new().unwrap();
        let a = dir.path().join("a.txt");
        let b = dir.path().join("b.txt");
        std::fs::write(&a, b"AAA").unwrap();
        std::fs::write(&b, b"BBB").unwrap();
        swap_files(&a, &b).unwrap();
        assert_eq!(std::fs::read(&a).unwrap(), b"BBB");
        assert_eq!(std::fs::read(&b).unwrap(), b"AAA");
    }

    // ── create_no_compression_zip ─────────────────────────────────────────────

    #[test]
    fn zip_is_created_and_contains_files() {
        let dir = TempDir::new().unwrap();
        let folder = dir.path().join("bundle");
        std::fs::create_dir(&folder).unwrap();
        std::fs::write(folder.join("file1.txt"), b"hello").unwrap();
        std::fs::write(folder.join("file2.jpg"), b"fakeimg").unwrap();

        create_no_compression_zip(&folder).unwrap();

        let zip_path = dir.path().join("bundle.zip");
        assert!(zip_path.exists(), "zip file should be created");
        let zip_size = std::fs::metadata(&zip_path).unwrap().len();
        assert!(zip_size > 0, "zip file should not be empty");
    }

    // ── perform_batch_copy_and_encode (smoke test) ────────────────────────────

    #[test]
    fn batch_creates_copies_folder_and_subfolders() {
        let src = TempDir::new().unwrap();
        std::fs::write(src.path().join("data.txt"), b"content").unwrap();

        let opts = BatchOptions {
            source_folder: src.path().to_path_buf(),
            num_copies: 2,
            base_text: "ORDER 001".to_string(),
            add_swap: false,
            add_watermark: false,
            create_zip: false,
            watermark_text: None,
            photo_number: None,
            visible_size: None,
            visible_opacity: None,
            visible_scale: None,
            add_video_watermark: false,
            video_watermark_text: None,
            video_watermark_timestamp_sec: None,
            video_watermark_font_size: None,
            ffmpeg_path: None,
            ffprobe_path: None,
        };

        let copies_folder =
            perform_batch_copy_and_encode(opts, |_, _, _| {}, |_, _| {}, || false).unwrap();

        assert!(copies_folder.exists());
        // Two order subfolders: 001, 002
        assert!(copies_folder.join("001").exists());
        assert!(copies_folder.join("002").exists());

        // Clean up the copies folder (it lives next to the temp dir)
        let _ = std::fs::remove_dir_all(&copies_folder);
    }
}

fn extract_start_number(text: &str) -> u32 {
    let re = Regex::new(r"\d+$").ok();
    re.and_then(|rx| rx.find(text).and_then(|m| m.as_str().parse::<u32>().ok()))
        .unwrap_or(1)
}
