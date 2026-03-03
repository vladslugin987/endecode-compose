use serde::{Deserialize, Serialize};
use tauri::AppHandle;
use tauri_plugin_updater::UpdaterExt;

#[derive(Debug, Serialize)]
pub struct UpdateCheckResult {
    pub available: bool,
    pub version: Option<String>,
    pub notes: Option<String>,
    pub pub_date: Option<String>,
}

#[derive(Debug, Deserialize)]
pub struct InstallUpdateRequest {
    /// Version string that was returned by check_for_update — used to confirm
    /// the user intentionally chose to install this exact version.
    pub version: String,
}

/// Check whether a newer version is available on GitHub Releases.
/// Returns version info without downloading anything.
#[tauri::command]
pub async fn check_for_update(app: AppHandle) -> Result<UpdateCheckResult, String> {
    let updater = app
        .updater()
        .map_err(|e| format!("Updater not available: {e}"))?;

    match updater.check().await {
        Ok(Some(update)) => Ok(UpdateCheckResult {
            available: true,
            version: Some(update.version.clone()),
            notes: update.body.clone(),
            pub_date: update.date.map(|d| d.to_string()),
        }),
        Ok(None) => Ok(UpdateCheckResult {
            available: false,
            version: None,
            notes: None,
            pub_date: None,
        }),
        Err(e) => Err(format!("Update check failed: {e}")),
    }
}

/// Download and install the update, then restart the app.
/// The frontend is responsible for showing progress before calling this.
#[tauri::command]
pub async fn install_update(app: AppHandle) -> Result<(), String> {
    let updater = app
        .updater()
        .map_err(|e| format!("Updater not available: {e}"))?;

    let update = updater
        .check()
        .await
        .map_err(|e| format!("Update check failed: {e}"))?
        .ok_or("No update available")?;

    update
        .download_and_install(|_chunk, _total| {}, || {})
        .await
        .map_err(|e| format!("Install failed: {e}"))?;

    // Restart is triggered from the frontend after this returns Ok.
    Ok(())
}
