use std::{
    fs,
    path::{Path, PathBuf},
};
use walkdir::WalkDir;

pub const SUPPORTED_EXTENSIONS: &[&str] =
    &["txt", "jpg", "jpeg", "png", "mp4", "m4v", "avi", "mov", "mkv", "webm"];
pub const VIDEO_EXTENSIONS: &[&str] = &["mp4", "m4v", "avi", "mov", "mkv", "webm"];
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

#[cfg(test)]
mod tests {
    use super::*;
    use std::fs;
    use tempfile::TempDir;

    fn tmp_file(dir: &TempDir, name: &str) -> PathBuf {
        let p = dir.path().join(name);
        fs::write(&p, b"x").unwrap();
        p
    }

    // ── extension helpers ────────────────────────────────────────────────────

    #[test]
    fn image_extensions_recognised() {
        let dir = TempDir::new().unwrap();
        for ext in ["jpg", "jpeg", "png"] {
            let p = tmp_file(&dir, &format!("file.{ext}"));
            assert!(is_image_file(&p), "{ext} should be image");
        }
    }

    #[test]
    fn video_extensions_recognised() {
        let dir = TempDir::new().unwrap();
        for ext in ["mp4", "m4v", "mov", "avi", "mkv", "webm"] {
            let p = tmp_file(&dir, &format!("file.{ext}"));
            assert!(is_video_file(&p), "{ext} should be video");
        }
    }

    #[test]
    fn image_is_not_video_and_vice_versa() {
        let dir = TempDir::new().unwrap();
        let img = tmp_file(&dir, "photo.jpg");
        let vid = tmp_file(&dir, "clip.mp4");
        assert!(!is_video_file(&img));
        assert!(!is_image_file(&vid));
    }

    #[test]
    fn unknown_extension_not_supported() {
        let dir = TempDir::new().unwrap();
        let p = tmp_file(&dir, "archive.rar");
        assert!(!is_supported_file(&p));
        assert!(!is_image_file(&p));
        assert!(!is_video_file(&p));
    }

    #[test]
    fn txt_is_supported_but_not_image_or_video() {
        let dir = TempDir::new().unwrap();
        let p = tmp_file(&dir, "notes.txt");
        assert!(is_supported_file(&p));
        assert!(!is_image_file(&p));
        assert!(!is_video_file(&p));
    }

    // ── extension case-insensitivity ─────────────────────────────────────────

    #[test]
    fn extensions_are_case_insensitive() {
        let dir = TempDir::new().unwrap();
        let p = tmp_file(&dir, "photo.JPG");
        assert!(is_image_file(&p));
        let p = tmp_file(&dir, "clip.MP4");
        assert!(is_video_file(&p));
    }

    // ── get_supported_files ───────────────────────────────────────────────────

    #[test]
    fn get_supported_files_finds_expected_files() {
        let dir = TempDir::new().unwrap();
        tmp_file(&dir, "a.txt");
        tmp_file(&dir, "b.jpg");
        tmp_file(&dir, "c.mp4");
        tmp_file(&dir, "d.rar"); // not supported
        let found = get_supported_files(dir.path());
        assert_eq!(found.len(), 3);
    }

    #[test]
    fn get_supported_files_recurses_into_subdirs() {
        let dir = TempDir::new().unwrap();
        let sub = dir.path().join("sub");
        fs::create_dir(&sub).unwrap();
        fs::write(sub.join("deep.png"), b"x").unwrap();
        tmp_file(&dir, "top.txt");
        let found = get_supported_files(dir.path());
        assert_eq!(found.len(), 2);
    }

    // ── copy_directory ────────────────────────────────────────────────────────

    #[test]
    fn copy_directory_replicates_structure() {
        let src = TempDir::new().unwrap();
        let dst = TempDir::new().unwrap();
        let sub = src.path().join("nested");
        fs::create_dir(&sub).unwrap();
        fs::write(sub.join("file.txt"), b"hello").unwrap();
        fs::write(src.path().join("root.jpg"), b"img").unwrap();

        copy_directory(src.path(), dst.path()).unwrap();

        assert!(dst.path().join("root.jpg").exists());
        assert!(dst.path().join("nested").join("file.txt").exists());
        let content = fs::read(dst.path().join("nested").join("file.txt")).unwrap();
        assert_eq!(content, b"hello");
    }
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
