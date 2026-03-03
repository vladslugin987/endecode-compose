use std::{fs, path::PathBuf};

use serde::{Deserialize, Serialize};
use tauri::AppHandle;
use uuid::Uuid;

use crate::{
    core::{encoding, fs_utils, watermark},
    events::{emit_done, emit_log, emit_progress, now_iso, JobDoneEvent, JobLogEvent, JobProgressEvent, LogLevel},
    state::AppState,
};

#[derive(Debug, Deserialize)]
pub struct DecryptRequest {
    pub folder_path: String,
}

#[derive(Debug, Serialize)]
pub struct DecryptResponse {
    pub job_id: String,
    pub total_files: usize,
}

#[tauri::command]
pub async fn decrypt_folder(
    app: AppHandle,
    state: tauri::State<'_, AppState>,
    payload: DecryptRequest,
) -> Result<DecryptResponse, String> {
    let folder = PathBuf::from(&payload.folder_path);
    if !folder.exists() || !folder.is_dir() {
        return Err("Selected folder does not exist".to_string());
    }

    let job_id = Uuid::new_v4().to_string();
    state.register_job(&job_id);
    let state = state.inner().clone();
    let files = fs_utils::get_supported_files(&folder);
    let total_files = files.len();

    let app_clone = app.clone();
    let job_id_clone = job_id.clone();
    tauri::async_runtime::spawn(async move {
        emit_log(
            &app_clone,
            JobLogEvent {
                job_id: job_id_clone.clone(),
                level: LogLevel::Info,
                message: format!("Starting decryption, files: {total_files}"),
                ts: now_iso(),
            },
        );

        for (index, file) in files.iter().enumerate() {
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

            match watermark::extract_watermark_text(file) {
                Ok(Some(raw)) => {
                    let decoded = encoding::decode_text(&raw);
                    emit_log(
                        &app_clone,
                        JobLogEvent {
                            job_id: job_id_clone.clone(),
                            level: LogLevel::Info,
                            message: format!("{} [new format]: {}", file.display(), decoded),
                            ts: now_iso(),
                        },
                    );
                }
                Ok(None) => {
                    let fallback = fs::read_to_string(file).unwrap_or_default();
                    let message = if fallback.contains("<<=") || fallback.contains("=>>") {
                        format!("{}: Partial watermark found, might be corrupted", file.display())
                    } else {
                        format!("{}: No watermark found", file.display())
                    };
                    emit_log(
                        &app_clone,
                        JobLogEvent {
                            job_id: job_id_clone.clone(),
                            level: LogLevel::Warn,
                            message,
                            ts: now_iso(),
                        },
                    );
                }
                Err(err) => {
                    emit_log(
                        &app_clone,
                        JobLogEvent {
                            job_id: job_id_clone.clone(),
                            level: LogLevel::Error,
                            message: format!("Error decoding {}: {err}", file.display()),
                            ts: now_iso(),
                        },
                    );
                }
            }

            let progress = (index + 1) as f32 / total_files.max(1) as f32;
            emit_progress(
                &app_clone,
                JobProgressEvent {
                    job_id: job_id_clone.clone(),
                    progress,
                    current_file: Some(file.display().to_string()),
                    stage: Some("decrypt".to_string()),
                },
            );
        }

        emit_done(
            &app_clone,
            JobDoneEvent {
                job_id: job_id_clone.clone(),
                status: "ok".to_string(),
                summary: Some(serde_json::json!({ "total_files": total_files })),
                error: None,
            },
        );
        state.finish_job(&job_id_clone);
    });

    Ok(DecryptResponse { job_id, total_files })
}
