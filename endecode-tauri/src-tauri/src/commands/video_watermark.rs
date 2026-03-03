use std::path::PathBuf;

use serde::{Deserialize, Serialize};
use tauri::AppHandle;
use uuid::Uuid;

use crate::{
    core::{fs_utils, video_watermark, video_watermark::VideoWatermarkOptions},
    events::{
        emit_done, emit_log, emit_progress, now_iso, JobDoneEvent, JobLogEvent,
        JobProgressEvent, LogLevel,
    },
    state::AppState,
};

#[derive(Debug, Deserialize)]
pub struct VideoWatermarkRequest {
    /// Path to the folder containing video files.
    pub folder_path: String,
    /// Text to burn into frames (e.g. order number "001").
    pub text: String,
    /// Timestamp in seconds where the watermark segment starts.
    /// None = auto (60 s or 20 % of duration, whichever is smaller).
    pub timestamp_sec: Option<f64>,
    /// Font size for drawtext (default 16).
    pub font_size: Option<u32>,
}

#[derive(Debug, Serialize)]
pub struct VideoWatermarkResponse {
    pub job_id: String,
    pub total_videos: usize,
}

#[tauri::command]
pub async fn add_video_watermark(
    app: AppHandle,
    state: tauri::State<'_, AppState>,
    payload: VideoWatermarkRequest,
) -> Result<VideoWatermarkResponse, String> {
    let folder = PathBuf::from(&payload.folder_path);
    if !folder.exists() || !folder.is_dir() {
        return Err("Selected folder does not exist".into());
    }
    if payload.text.trim().is_empty() {
        return Err("Watermark text is empty".into());
    }
    if !video_watermark::check_ffmpeg_available(&app) {
        return Err(
            "FFmpeg not found. Install FFmpeg (brew install ffmpeg) or place the binary \
             next to the application."
                .into(),
        );
    }

    let video_files: Vec<PathBuf> = fs_utils::get_supported_files(&folder)
        .into_iter()
        .filter(|p| fs_utils::is_video_file(p))
        .collect();

    if video_files.is_empty() {
        return Err("No video files found in the selected folder".into());
    }

    let total = video_files.len();
    let job_id = Uuid::new_v4().to_string();
    state.register_job(&job_id);
    let state = state.inner().clone();

    let opts = VideoWatermarkOptions {
        text: payload.text.trim().to_string(),
        timestamp_sec: payload.timestamp_sec,
        font_size: payload.font_size.unwrap_or(16).clamp(8, 64),
    };

    let ffmpeg = video_watermark::find_ffmpeg(&app);
    let ffprobe = video_watermark::find_ffprobe(&app);
    let app_clone = app.clone();
    let job_id_clone = job_id.clone();

    tauri::async_runtime::spawn(async move {
        let mut processed = 0_usize;
        let mut errors = 0_usize;

        for (idx, file) in video_files.iter().enumerate() {
            if state.is_cancelled(&job_id_clone) {
                emit_done(
                    &app_clone,
                    JobDoneEvent {
                        job_id: job_id_clone.clone(),
                        status: "cancelled".into(),
                        summary: None,
                        error: None,
                    },
                );
                state.finish_job(&job_id_clone);
                return;
            }

            let file_name = file
                .file_name()
                .unwrap_or_default()
                .to_string_lossy()
                .into_owned();

            emit_log(
                &app_clone,
                JobLogEvent {
                    job_id: job_id_clone.clone(),
                    level: LogLevel::Info,
                    message: format!("Processing: {file_name}"),
                    ts: now_iso(),
                },
            );

            match video_watermark::add_video_watermark(&ffmpeg, &ffprobe, file, &opts) {
                Ok(()) => {
                    processed += 1;
                    emit_log(
                        &app_clone,
                        JobLogEvent {
                            job_id: job_id_clone.clone(),
                            level: LogLevel::Success,
                            message: format!("Watermark added: {file_name}"),
                            ts: now_iso(),
                        },
                    );
                }
                Err(e) => {
                    errors += 1;
                    emit_log(
                        &app_clone,
                        JobLogEvent {
                            job_id: job_id_clone.clone(),
                            level: LogLevel::Error,
                            message: format!("Failed [{file_name}]: {e}"),
                            ts: now_iso(),
                        },
                    );
                }
            }

            emit_progress(
                &app_clone,
                JobProgressEvent {
                    job_id: job_id_clone.clone(),
                    progress: (idx + 1) as f32 / total.max(1) as f32,
                    current_file: Some(file.display().to_string()),
                    stage: Some("video_watermark".into()),
                },
            );
        }

        emit_done(
            &app_clone,
            JobDoneEvent {
                job_id: job_id_clone.clone(),
                status: if errors == 0 { "ok" } else { "error" }.into(),
                summary: Some(serde_json::json!({
                    "total_videos": total,
                    "processed": processed,
                    "errors": errors,
                })),
                error: if errors > 0 {
                    Some(format!("{errors} video(s) failed"))
                } else {
                    None
                },
            },
        );
        state.finish_job(&job_id_clone);
    });

    Ok(VideoWatermarkResponse {
        job_id,
        total_videos: total,
    })
}
