use serde::{Deserialize, Serialize};

use crate::state::AppState;

#[derive(Debug, Deserialize)]
pub struct CancelRequest {
    pub job_id: String,
}

#[derive(Debug, Serialize)]
pub struct CancelResponse {
    pub cancelled: bool,
}

#[tauri::command]
pub async fn cancel_job(
    state: tauri::State<'_, AppState>,
    payload: CancelRequest,
) -> Result<CancelResponse, String> {
    Ok(CancelResponse {
        cancelled: state.cancel_job(&payload.job_id),
    })
}
