use std::path::PathBuf;

use serde::{Deserialize, Serialize};
use tauri::AppHandle;
use uuid::Uuid;

use crate::{
    core::{encoding, fs_utils, watermark},
    events::{emit_done, emit_log, emit_progress, now_iso, JobDoneEvent, JobLogEvent, JobProgressEvent, LogLevel},
    state::AppState,
};

#[derive(Debug, Deserialize)]
pub struct EncryptRequest {
    pub folder_path: String,
    pub inject_name: String,
}

#[derive(Debug, Serialize)]
pub struct EncryptResponse {
    pub job_id: String,
    pub total_files: usize,
}

#[tauri::command]
pub async fn encrypt_folder(
    app: AppHandle,
    state: tauri::State<'_, AppState>,
    payload: EncryptRequest,
) -> Result<EncryptResponse, String> {
    let folder = PathBuf::from(&payload.folder_path);
    if !folder.exists() || !folder.is_dir() {
        return Err("Selected folder does not exist".to_string());
    }
    if payload.inject_name.trim().is_empty() {
        return Err("Inject name is empty".to_string());
    }

    let job_id = Uuid::new_v4().to_string();
    state.register_job(&job_id);
    let state = state.inner().clone();
    let files = fs_utils::get_supported_files(&folder);
    let total_files = files.len();
    let text_watermark = encoding::add_watermark(&payload.inject_name);
    let encoded_text = encoding::encode_text(&payload.inject_name);

    let app_clone = app.clone();
    let job_id_clone = job_id.clone();
    tauri::async_runtime::spawn(async move {
        emit_log(
            &app_clone,
            JobLogEvent {
                job_id: job_id_clone.clone(),
                level: LogLevel::Info,
                message: format!("Starting encryption, files: {total_files}"),
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

            // Keep text-file behavior and use byte-tail watermark for binary media files.
            let result = if file
                .extension()
                .and_then(|ext| ext.to_str())
                .map(|ext| ext.eq_ignore_ascii_case("txt"))
                .unwrap_or(false)
            {
                encoding::process_file(file, &text_watermark)
            } else {
                watermark::add_watermark(file, &encoded_text)
            };
            match result {
                Ok(true) => emit_log(
                    &app_clone,
                    JobLogEvent {
                        job_id: job_id_clone.clone(),
                        level: LogLevel::Success,
                        message: format!("{}: Success", file.display()),
                        ts: now_iso(),
                    },
                ),
                Ok(false) => emit_log(
                    &app_clone,
                    JobLogEvent {
                        job_id: job_id_clone.clone(),
                        level: LogLevel::Warn,
                        message: format!("{}: Already contains watermark", file.display()),
                        ts: now_iso(),
                    },
                ),
                Err(err) => emit_log(
                    &app_clone,
                    JobLogEvent {
                        job_id: job_id_clone.clone(),
                        level: LogLevel::Error,
                        message: format!("{}: {err}", file.display()),
                        ts: now_iso(),
                    },
                ),
            }

            let progress = (index + 1) as f32 / total_files.max(1) as f32;
            emit_progress(
                &app_clone,
                JobProgressEvent {
                    job_id: job_id_clone.clone(),
                    progress,
                    current_file: Some(file.display().to_string()),
                    stage: Some("encrypt".to_string()),
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

    Ok(EncryptResponse { job_id, total_files })
}
