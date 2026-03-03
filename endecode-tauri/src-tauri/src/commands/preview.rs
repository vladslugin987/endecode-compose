use std::path::PathBuf;

use base64::{engine::general_purpose, Engine as _};
use image::ImageFormat;
use serde::{Deserialize, Serialize};

use crate::core::fs_utils;

#[derive(Debug, Deserialize)]
pub struct PreviewRequest {
    pub folder_path: String,
}

#[derive(Debug, Serialize)]
pub struct PreviewResponse {
    pub image_path: Option<String>,
    pub image_data_url: Option<String>,
}

#[tauri::command]
pub async fn preview_first_image(payload: PreviewRequest) -> Result<PreviewResponse, String> {
    let folder = PathBuf::from(payload.folder_path);
    if !folder.exists() || !folder.is_dir() {
        return Ok(PreviewResponse {
            image_path: None,
            image_data_url: None,
        });
    }

    let first = fs_utils::get_supported_files(&folder)
        .into_iter()
        .filter(|p| fs_utils::is_image_file(p))
        .min_by_key(|p| p.file_name().map(|v| v.to_os_string()));

    if let Some(path) = first {
        let data_url = build_preview_data_url(&path).ok();
        return Ok(PreviewResponse {
            image_path: Some(path.display().to_string()),
            image_data_url: data_url,
        });
    }

    Ok(PreviewResponse {
        image_path: None,
        image_data_url: None,
    })
}

fn build_preview_data_url(path: &PathBuf) -> Result<String, String> {
    let img = image::open(path)
        .map_err(|e| format!("Failed to load preview image {}: {e}", path.display()))?;
    let resized = img.resize(1024, 1024, image::imageops::FilterType::Triangle);
    let mut out = std::io::Cursor::new(Vec::<u8>::new());
    resized
        .write_to(&mut out, ImageFormat::Png)
        .map_err(|e| format!("Failed to encode preview image {}: {e}", path.display()))?;
    let b64 = general_purpose::STANDARD.encode(out.into_inner());
    Ok(format!("data:image/png;base64,{b64}"))
}
