use serde::Serialize;
use tauri::{AppHandle, Emitter};

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "snake_case")]
pub enum LogLevel {
    Info,
    Warn,
    Error,
    Success,
}

#[derive(Debug, Clone, Serialize)]
pub struct JobLogEvent {
    pub job_id: String,
    pub level: LogLevel,
    pub message: String,
    pub ts: String,
}

#[derive(Debug, Clone, Serialize)]
pub struct JobProgressEvent {
    pub job_id: String,
    pub progress: f32,
    pub current_file: Option<String>,
    pub stage: Option<String>,
}

#[derive(Debug, Clone, Serialize)]
pub struct JobDoneEvent {
    pub job_id: String,
    pub status: String,
    pub summary: Option<serde_json::Value>,
    pub error: Option<String>,
}

pub fn emit_log(app: &AppHandle, payload: JobLogEvent) {
    let _ = app.emit("job://log", payload);
}

pub fn emit_progress(app: &AppHandle, payload: JobProgressEvent) {
    let _ = app.emit("job://progress", payload);
}

pub fn emit_done(app: &AppHandle, payload: JobDoneEvent) {
    let _ = app.emit("job://done", payload);
}

pub fn now_iso() -> String {
    format!("{:?}", std::time::SystemTime::now())
}
