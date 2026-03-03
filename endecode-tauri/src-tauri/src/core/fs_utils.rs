use std::{
    fs,
    path::{Path, PathBuf},
};
use walkdir::WalkDir;

pub const SUPPORTED_EXTENSIONS: &[&str] = &["txt", "jpg", "jpeg", "png", "mp4", "avi", "mov", "mkv"];
pub const VIDEO_EXTENSIONS: &[&str] = &["mp4", "avi", "mov", "mkv"];
pub const IMAGE_EXTENSIONS: &[&str] = &["jpg", "jpeg", "png"];

fn ext_lower(path: &Path) -> Option<String> {
    path.extension()
        .and_then(|ext| ext.to_str())
        .map(|s| s.to_lowercase())
}

pub fn is_supported_file(path: &Path) -> bool {
    ext_lower(path)
        .map(|ext| SUPPORTED_EXTENSIONS.contains(&ext.as_str()))
        .unwrap_or(false)
}

pub fn is_image_file(path: &Path) -> bool {
    ext_lower(path)
        .map(|ext| IMAGE_EXTENSIONS.contains(&ext.as_str()))
        .unwrap_or(false)
}

pub fn is_video_file(path: &Path) -> bool {
    ext_lower(path)
        .map(|ext| VIDEO_EXTENSIONS.contains(&ext.as_str()))
        .unwrap_or(false)
}

pub fn get_supported_files(directory: &Path) -> Vec<PathBuf> {
    WalkDir::new(directory)
        .into_iter()
        .filter_map(Result::ok)
        .map(|entry| entry.path().to_path_buf())
        .filter(|path| path.is_file() && is_supported_file(path))
        .collect()
}

pub fn count_files(directory: &Path) -> usize {
    get_supported_files(directory).len()
}

pub fn copy_directory(source: &Path, destination: &Path) -> Result<(), String> {
    if !destination.exists() {
        fs::create_dir_all(destination)
            .map_err(|e| format!("Error creating destination {}: {e}", destination.display()))?;
    }

    for entry in WalkDir::new(source).into_iter().filter_map(Result::ok) {
        let path = entry.path();
        let relative = path
            .strip_prefix(source)
            .map_err(|e| format!("Error building relative path: {e}"))?;
        let target = destination.join(relative);

        if path.is_dir() {
            fs::create_dir_all(&target)
                .map_err(|e| format!("Error creating directory {}: {e}", target.display()))?;
        } else {
            if let Some(parent) = target.parent() {
                fs::create_dir_all(parent)
                    .map_err(|e| format!("Error creating parent {}: {e}", parent.display()))?;
            }
            fs::copy(path, &target).map_err(|e| {
                format!("Error copying file {} -> {}: {e}", path.display(), target.display())
            })?;
        }
    }

    Ok(())
}
