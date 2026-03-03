use std::{
    fs::{self, File},
    io::{Read, Write},
    path::{Path, PathBuf},
};

use regex::Regex;
use zip::{write::SimpleFileOptions, CompressionMethod, ZipWriter};

use super::{encoding, fs_utils, image_text::TextPosition, watermark};

#[derive(Debug, Clone)]
pub struct BatchOptions {
    pub source_folder: PathBuf,
    pub num_copies: usize,
    pub base_text: String,
    pub add_swap: bool,
    pub add_watermark: bool,
    pub create_zip: bool,
    pub watermark_text: Option<String>,
    pub photo_number: Option<u32>,
}

pub fn perform_batch_copy_and_encode<F, C>(
    options: BatchOptions,
    mut on_progress: F,
    mut is_cancelled: C,
) -> Result<PathBuf, String>
where
    F: FnMut(f32, Option<String>, Option<String>),
    C: FnMut() -> bool,
{
    let source_folder = options.source_folder;
    let copies_folder = source_folder
        .parent()
        .unwrap_or_else(|| Path::new("."))
        .join(format!(
            "{}-Copies",
            source_folder.file_name().and_then(|n| n.to_str()).unwrap_or("Source")
        ));
    if !copies_folder.exists() {
        fs::create_dir_all(&copies_folder)
            .map_err(|e| format!("Error creating copies folder {}: {e}", copies_folder.display()))?;
    }

    let start_number = extract_start_number(&options.base_text);
    let base_text_without_number = Regex::new(r"\d+$")
        .map_err(|e| format!("Regex error: {e}"))?
        .replace(options.base_text.trim(), "")
        .to_string()
        .trim()
        .to_string();

    let mut total_ops = options.num_copies as f32 * 2.0;
    if options.add_watermark {
        total_ops += options.num_copies as f32;
    }
    if options.add_swap {
        total_ops += options.num_copies as f32;
    }
    if options.create_zip {
        total_ops += options.num_copies as f32;
    }
    let mut completed = 0.0_f32;
    let mut folders_to_zip: Vec<PathBuf> = Vec::new();

    for i in 0..options.num_copies {
        if is_cancelled() {
            return Err("Cancelled".to_string());
        }
        let order_number = format!("{:03}", start_number + i as u32);
        let order_folder = copies_folder.join(&order_number);
        fs::create_dir_all(&order_folder)
            .map_err(|e| format!("Error creating order folder {}: {e}", order_folder.display()))?;
        let destination = order_folder.join(
            source_folder
                .file_name()
                .and_then(|v| v.to_str())
                .unwrap_or("Bundle"),
        );

        fs_utils::copy_directory(&source_folder, &destination)?;
        completed += 1.0;
        on_progress(completed / total_ops, None, Some("copy".to_string()));

        process_files(&destination, &base_text_without_number, &order_number)?;
        completed += 1.0;
        on_progress(completed / total_ops, None, Some("encode".to_string()));

        if options.add_watermark {
            let actual_photo_number = options.photo_number.unwrap_or(order_number.parse().unwrap_or(1));
            let actual_text = options
                .watermark_text
                .clone()
                .unwrap_or_else(|| order_number.clone());
            add_visible_watermark_to_photo(&destination, &actual_text, actual_photo_number)?;
            completed += 1.0;
            on_progress(completed / total_ops, None, Some("watermark".to_string()));
        }

        if options.add_swap {
            perform_swap(&destination, &order_number)?;
            completed += 1.0;
            on_progress(completed / total_ops, None, Some("swap".to_string()));
        }

        folders_to_zip.push(destination);
    }

    if options.create_zip {
        for folder in &folders_to_zip {
            if is_cancelled() {
                return Err("Cancelled".to_string());
            }
            create_no_compression_zip(folder)?;
            fs::remove_dir_all(folder)
                .map_err(|e| format!("Error removing folder {}: {e}", folder.display()))?;
            completed += 1.0;
            on_progress(completed / total_ops, None, Some("zip".to_string()));
        }
    }

    Ok(copies_folder)
}

fn process_files(folder: &Path, base_text: &str, order_number: &str) -> Result<(), String> {
    let files = fs_utils::get_supported_files(folder);
    let encoded_text = format!("{base_text} {order_number}");
    let encoded_watermark = encoding::encode_text(&encoded_text);
    let watermark_text = encoding::add_watermark(&encoded_text);

    for file in files {
        if fs_utils::is_video_file(&file) {
            let _ = watermark::add_watermark(&file, &encoded_watermark)?;
        } else {
            let _ = encoding::process_file(&file, &watermark_text)?;
        }
    }

    Ok(())
}

fn add_visible_watermark_to_photo(folder: &Path, watermark_text: &str, photo_number: u32) -> Result<(), String> {
    let files = fs_utils::get_supported_files(folder);
    for file in files.into_iter().filter(|p| fs_utils::is_image_file(p)) {
        if extract_file_number(file.file_name().and_then(|n| n.to_str()).unwrap_or_default())
            == Some(photo_number)
        {
            let _ =
                super::image_text::add_text_to_image(&file, watermark_text, TextPosition::BottomRight)?;
            break;
        }
    }
    Ok(())
}

fn perform_swap(folder: &Path, order_number: &str) -> Result<(), String> {
    let base_number: u32 = order_number.parse().unwrap_or(1);
    let swap_number = base_number + 10;
    let all_images: Vec<PathBuf> = fs_utils::get_supported_files(folder)
        .into_iter()
        .filter(|p| fs_utils::is_image_file(p))
        .collect();

    let file_a = all_images
        .iter()
        .find(|p| extract_file_number(p.file_name().and_then(|n| n.to_str()).unwrap_or_default()) == Some(base_number))
        .cloned();
    let file_b = all_images
        .iter()
        .find(|p| extract_file_number(p.file_name().and_then(|n| n.to_str()).unwrap_or_default()) == Some(swap_number))
        .cloned();

    if let (Some(a), Some(b)) = (file_a, file_b) {
        swap_files(&a, &b)?;
    }
    Ok(())
}

fn swap_files(file_a: &Path, file_b: &Path) -> Result<(), String> {
    let temp = file_a.with_file_name(format!(
        "temp_{}_{}",
        std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map(|d| d.as_millis())
            .unwrap_or(0),
        file_a.file_name().and_then(|n| n.to_str()).unwrap_or("a")
    ));
    fs::rename(file_a, &temp).map_err(|e| format!("Error rename A->temp: {e}"))?;
    fs::rename(file_b, file_a).map_err(|e| format!("Error rename B->A: {e}"))?;
    fs::rename(&temp, file_b).map_err(|e| format!("Error rename temp->B: {e}"))?;
    Ok(())
}

fn create_no_compression_zip(folder_to_zip: &Path) -> Result<(), String> {
    let zip_file = folder_to_zip
        .parent()
        .unwrap_or_else(|| Path::new("."))
        .join(format!(
            "{}.zip",
            folder_to_zip.file_name().and_then(|n| n.to_str()).unwrap_or("bundle")
        ));
    let file = File::create(&zip_file)
        .map_err(|e| format!("Error creating zip {}: {e}", zip_file.display()))?;
    let mut zip = ZipWriter::new(file);
    let options = SimpleFileOptions::default().compression_method(CompressionMethod::Stored);

    for entry in walkdir::WalkDir::new(folder_to_zip).into_iter().filter_map(Result::ok) {
        let path = entry.path();
        let name = path
            .strip_prefix(folder_to_zip)
            .map_err(|e| format!("Path strip error: {e}"))?
            .to_string_lossy()
            .replace('\\', "/");

        if path.is_dir() {
            if !name.is_empty() {
                zip.add_directory(format!("{name}/"), options)
                    .map_err(|e| format!("Zip add directory error: {e}"))?;
            }
        } else {
            zip.start_file(name, options)
                .map_err(|e| format!("Zip start file error: {e}"))?;
            let mut f = File::open(path).map_err(|e| format!("Open file error: {e}"))?;
            let mut buffer = Vec::new();
            f.read_to_end(&mut buffer)
                .map_err(|e| format!("Read file error: {e}"))?;
            zip.write_all(&buffer)
                .map_err(|e| format!("Zip write error: {e}"))?;
        }
    }

    zip.finish().map_err(|e| format!("Zip finish error: {e}"))?;
    Ok(())
}

fn extract_file_number(filename: &str) -> Option<u32> {
    let re = Regex::new(r".*?(\d+).*").ok()?;
    re.captures(filename)
        .and_then(|c| c.get(1))
        .and_then(|m| m.as_str().parse::<u32>().ok())
}

fn extract_start_number(text: &str) -> u32 {
    let re = Regex::new(r"\d+$").ok();
    re.and_then(|rx| rx.find(text).and_then(|m| m.as_str().parse::<u32>().ok()))
        .unwrap_or(1)
}
