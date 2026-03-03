use std::path::PathBuf;

use serde::{Deserialize, Serialize};
use tauri::AppHandle;
use uuid::Uuid;

use crate::{
    core::{encoding, fs_utils},
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
    let files = fs_utils::get_supported_files(&folder);
    let total_files = files.len();
    let watermark = encoding::add_watermark(&payload.inject_name);

    emit_log(
        &app,
        JobLogEvent {
            job_id: job_id.clone(),
            level: LogLevel::Info,
            message: format!("Starting encryption, files: {total_files}"),
            ts: now_iso(),
        },
    );

    for (index, file) in files.iter().enumerate() {
        if state.is_cancelled(&job_id) {
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
            return Ok(EncryptResponse { job_id, total_files });
        }

        let result = encoding::process_file(file, &watermark);
        match result {
            Ok(true) => emit_log(
                &app,
                JobLogEvent {
                    job_id: job_id.clone(),
                    level: LogLevel::Success,
                    message: format!("{}: Success", file.display()),
                    ts: now_iso(),
                },
            ),
            Ok(false) => emit_log(
                &app,
                JobLogEvent {
                    job_id: job_id.clone(),
                    level: LogLevel::Warn,
                    message: format!("{}: Already contains watermark", file.display()),
                    ts: now_iso(),
                },
            ),
            Err(err) => emit_log(
                &app,
                JobLogEvent {
                    job_id: job_id.clone(),
                    level: LogLevel::Error,
                    message: format!("{}: {err}", file.display()),
                    ts: now_iso(),
                },
            ),
        }

        let progress = (index + 1) as f32 / total_files.max(1) as f32;
        emit_progress(
            &app,
            JobProgressEvent {
                job_id: job_id.clone(),
                progress,
                current_file: Some(file.display().to_string()),
                stage: Some("encrypt".to_string()),
            },
        );
    }

    emit_done(
        &app,
        JobDoneEvent {
            job_id: job_id.clone(),
            status: "ok".to_string(),
            summary: Some(serde_json::json!({ "total_files": total_files })),
            error: None,
        },
    );
    state.finish_job(&job_id);
    Ok(EncryptResponse { job_id, total_files })
}
