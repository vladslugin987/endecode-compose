use std::path::PathBuf;

use serde::{Deserialize, Serialize};
use tauri::AppHandle;
use uuid::Uuid;

use crate::{
    core::batch::{self, BatchOptions},
    events::{emit_done, emit_log, emit_progress, now_iso, JobDoneEvent, JobLogEvent, JobProgressEvent, LogLevel},
    state::AppState,
};

#[derive(Debug, Deserialize)]
pub struct BatchRequest {
    pub source_folder: String,
    pub num_copies: usize,
    pub base_text: String,
    pub add_swap: bool,
    pub add_watermark: bool,
    pub create_zip: bool,
    pub watermark_text: Option<String>,
    pub photo_number: Option<u32>,
    pub visible_size: Option<String>,
    pub visible_opacity: Option<u8>,
}

#[derive(Debug, Serialize)]
pub struct BatchResponse {
    pub job_id: String,
    pub output_folder: String,
}

#[tauri::command]
pub async fn batch_copy(
    app: AppHandle,
    state: tauri::State<'_, AppState>,
    payload: BatchRequest,
) -> Result<BatchResponse, String> {
    let source_folder = PathBuf::from(&payload.source_folder);
    if !source_folder.exists() || !source_folder.is_dir() {
        return Err("Source folder does not exist".to_string());
    }

    let job_id = Uuid::new_v4().to_string();
    state.register_job(&job_id);
    let state = state.inner().clone();
    let output_folder = source_folder
        .parent()
        .unwrap_or_else(|| std::path::Path::new("."))
        .join(format!(
            "{}-Copies",
            source_folder
                .file_name()
                .and_then(|n| n.to_str())
                .unwrap_or("Source")
        ))
        .display()
        .to_string();
    emit_log(
        &app,
        JobLogEvent {
            job_id: job_id.clone(),
            level: LogLevel::Info,
            message: "Starting batch copy process".to_string(),
            ts: now_iso(),
        },
    );

    let options = BatchOptions {
        source_folder: source_folder.clone(),
        num_copies: payload.num_copies,
        base_text: payload.base_text,
        add_swap: payload.add_swap,
        add_watermark: payload.add_watermark,
        create_zip: payload.create_zip,
        watermark_text: payload.watermark_text,
        photo_number: payload.photo_number,
        visible_size: payload.visible_size,
        visible_opacity: payload.visible_opacity,
    };

    let app_clone = app.clone();
    let job_id_clone = job_id.clone();
    let output_folder_clone = output_folder.clone();
    tauri::async_runtime::spawn(async move {
        let result = batch::perform_batch_copy_and_encode(
            options,
            |progress, current_file, stage| {
                emit_progress(
                    &app_clone,
                    JobProgressEvent {
                        job_id: job_id_clone.clone(),
                        progress,
                        current_file,
                        stage,
                    },
                );
            },
            |level, message| {
                let mapped = match level {
                    "error" => LogLevel::Error,
                    "warn" => LogLevel::Warn,
                    "success" => LogLevel::Success,
                    _ => LogLevel::Info,
                };
                emit_log(
                    &app_clone,
                    JobLogEvent {
                        job_id: job_id_clone.clone(),
                        level: mapped,
                        message,
                        ts: now_iso(),
                    },
                );
            },
            || state.is_cancelled(&job_id_clone),
        );

        match result {
            Ok(output_folder_path) => {
                emit_done(
                    &app_clone,
                    JobDoneEvent {
                        job_id: job_id_clone.clone(),
                        status: "ok".to_string(),
                        summary: Some(
                            serde_json::json!({ "output_folder": output_folder_path.display().to_string() }),
                        ),
                        error: None,
                    },
                );
            }
            Err(err) if err == "Cancelled" => {
                emit_done(
                    &app_clone,
                    JobDoneEvent {
                        job_id: job_id_clone.clone(),
                        status: "cancelled".to_string(),
                        summary: None,
                        error: None,
                    },
                );
            }
            Err(err) => {
                emit_done(
                    &app_clone,
                    JobDoneEvent {
                        job_id: job_id_clone.clone(),
                        status: "error".to_string(),
                        summary: None,
                        error: Some(err),
                    },
                );
            }
        }
        state.finish_job(&job_id_clone);
    });

    Ok(BatchResponse {
        job_id,
        output_folder: output_folder_clone,
    })
}
