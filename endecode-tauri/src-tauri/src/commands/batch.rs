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
        source_folder,
        num_copies: payload.num_copies,
        base_text: payload.base_text,
        add_swap: payload.add_swap,
        add_watermark: payload.add_watermark,
        create_zip: payload.create_zip,
        watermark_text: payload.watermark_text,
        photo_number: payload.photo_number,
    };

    let result = batch::perform_batch_copy_and_encode(
        options,
        |progress, current_file, stage| {
            emit_progress(
                &app,
                JobProgressEvent {
                    job_id: job_id.clone(),
                    progress,
                    current_file,
                    stage,
                },
            );
        },
        || state.is_cancelled(&job_id),
    );

    match result {
        Ok(output_folder) => {
            emit_done(
                &app,
                JobDoneEvent {
                    job_id: job_id.clone(),
                    status: "ok".to_string(),
                    summary: Some(
                        serde_json::json!({ "output_folder": output_folder.display().to_string() }),
                    ),
                    error: None,
                },
            );
            state.finish_job(&job_id);
            Ok(BatchResponse {
                job_id,
                output_folder: output_folder.display().to_string(),
            })
        }
        Err(err) if err == "Cancelled" => {
            emit_done(
                &app,
                JobDoneEvent {
                    job_id: job_id.clone(),
                    status: "cancelled".to_string(),
                    summary: None,
                    error: None,
                },
            );
            state.finish_job(&job_id);
            Ok(BatchResponse {
                job_id,
                output_folder: String::new(),
            })
        }
        Err(err) => {
            emit_done(
                &app,
                JobDoneEvent {
                    job_id: job_id.clone(),
                    status: "error".to_string(),
                    summary: None,
                    error: Some(err.clone()),
                },
            );
            state.finish_job(&job_id);
            Err(err)
        }
    }
}
