use std::path::PathBuf;

use serde::{Deserialize, Serialize};
use tauri::AppHandle;
use uuid::Uuid;

use crate::{
    core::{
        fs_utils, image_text,
        image_text::{TextPosition, VisibleWatermarkSize, VisibleWatermarkStyle},
    },
    events::{emit_done, emit_log, emit_progress, now_iso, JobDoneEvent, JobLogEvent, JobProgressEvent, LogLevel},
    state::AppState,
};

#[derive(Debug, Deserialize)]
pub struct AddTextRequest {
    pub folder_path: String,
    pub text: String,
    pub photo_number: u32,
    pub visible_size: Option<String>,
    pub visible_opacity: Option<u8>,
    /// Direct pixel scale (1–24). Overrides visible_size when set.
    pub visible_scale: Option<u32>,
}

#[derive(Debug, Serialize)]
pub struct AddTextResponse {
    pub job_id: String,
    pub matched_files: usize,
    pub updated_files: usize,
}

#[tauri::command]
pub async fn add_text_to_photo(
    app: AppHandle,
    state: tauri::State<'_, AppState>,
    payload: AddTextRequest,
) -> Result<AddTextResponse, String> {
    let folder = PathBuf::from(&payload.folder_path);
    if !folder.exists() || !folder.is_dir() {
        return Err("Selected folder does not exist".to_string());
    }
    if payload.text.trim().is_empty() {
        return Err("Text is empty".to_string());
    }

    let job_id = Uuid::new_v4().to_string();
    state.register_job(&job_id);
    let state = state.inner().clone();
    let marker = format!("{:03}", payload.photo_number);
    let style = VisibleWatermarkStyle {
        size: VisibleWatermarkSize::from_optional_str(payload.visible_size.as_deref()),
        opacity: payload.visible_opacity.unwrap_or(200).clamp(30, 255),
        scale_override: payload.visible_scale.map(|s| s.clamp(1, 24)),
    };
    let image_files: Vec<PathBuf> = fs_utils::get_supported_files(&folder)
        .into_iter()
        .filter(|p| fs_utils::is_image_file(p))
        .collect();

    let mut matched_files: Vec<PathBuf> = image_files
        .iter()
        .filter(|f| {
            f.file_name()
                .and_then(|n| n.to_str())
                .map(|name| name.contains(&marker))
                .unwrap_or(false)
        })
        .cloned()
        .collect();
    if matched_files.is_empty() {
        if let Some(by_number) = image_files.iter().find(|f| {
            f.file_name()
                .and_then(|n| n.to_str())
                .map(|name| name.contains(&payload.photo_number.to_string()))
                .unwrap_or(false)
        }) {
            matched_files.push(by_number.clone());
        }
    }
    if matched_files.is_empty() {
        if let Some(first) = image_files.first() {
            matched_files.push(first.clone());
            emit_log(
                &app,
                JobLogEvent {
                    job_id: job_id.clone(),
                    level: LogLevel::Warn,
                    message: format!(
                        "No image matched photo number {}, fallback to first image {}",
                        payload.photo_number,
                        first.display()
                    ),
                    ts: now_iso(),
                },
            );
        }
    }

    let total = matched_files.len();
    let app_clone = app.clone();
    let job_id_clone = job_id.clone();
    let text = payload.text.clone();
    tauri::async_runtime::spawn(async move {
        let mut updated_files = 0_usize;
        for (idx, file) in matched_files.iter().enumerate() {
            if state.is_cancelled(&job_id_clone) {
                emit_done(
                    &app_clone,
                    JobDoneEvent {
                        job_id: job_id_clone.clone(),
                        status: "cancelled".to_string(),
                        summary: None,
                        error: None,
                    },
                );
                state.finish_job(&job_id_clone);
                return;
            }

            match image_text::add_text_to_image_with_style(file, &text, TextPosition::BottomRight, style) {
                Ok(true) => {
                    updated_files += 1;
                    emit_log(
                        &app_clone,
                        JobLogEvent {
                            job_id: job_id_clone.clone(),
                            level: LogLevel::Success,
                            message: format!("Added text to {}", file.display()),
                            ts: now_iso(),
                        },
                    );
                }
                Ok(false) => emit_log(
                    &app_clone,
                    JobLogEvent {
                        job_id: job_id_clone.clone(),
                        level: LogLevel::Warn,
                        message: format!("Skipped {}", file.display()),
                        ts: now_iso(),
                    },
                ),
                Err(err) => emit_log(
                    &app_clone,
                    JobLogEvent {
                        job_id: job_id_clone.clone(),
                        level: LogLevel::Error,
                        message: format!("Error processing {}: {err}", file.display()),
                        ts: now_iso(),
                    },
                ),
            }

            emit_progress(
                &app_clone,
                JobProgressEvent {
                    job_id: job_id_clone.clone(),
                    progress: (idx + 1) as f32 / total.max(1) as f32,
                    current_file: Some(file.display().to_string()),
                    stage: Some("add_text".to_string()),
                },
            );
        }

        emit_done(
            &app_clone,
            JobDoneEvent {
                job_id: job_id_clone.clone(),
                status: "ok".to_string(),
                summary: Some(
                    serde_json::json!({ "matched_files": total, "updated_files": updated_files }),
                ),
                error: None,
            },
        );
        state.finish_job(&job_id_clone);
    });

    Ok(AddTextResponse {
        job_id,
        matched_files: total,
        updated_files: 0,
    })
}
