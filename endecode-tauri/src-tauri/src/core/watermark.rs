use std::{
    fs::OpenOptions,
    io::{Read, Seek, SeekFrom, Write},
    path::Path,
};

const MAX_WATERMARK_LENGTH: usize = 100;
const WATERMARK_START: &[u8] = b"<<==";
const WATERMARK_END: &[u8] = b"==>>";

#[derive(Debug, Clone)]
struct WatermarkInfo {
    start_position: usize,
    end_position: usize,
    content: Option<Vec<u8>>,
}

fn find_bytes(data: &[u8], pattern: &[u8], start_from: usize, reverse: bool) -> Option<usize> {
    if pattern.is_empty() || data.len() < pattern.len() {
        return None;
    }

    if reverse {
        for idx in (start_from..=data.len() - pattern.len()).rev() {
            if &data[idx..idx + pattern.len()] == pattern {
                return Some(idx);
            }
        }
        None
    } else {
        for idx in start_from..=data.len() - pattern.len() {
            if &data[idx..idx + pattern.len()] == pattern {
                return Some(idx);
            }
        }
        None
    }
}

fn find_watermark(data: &[u8], include_content: bool) -> Option<WatermarkInfo> {
    let start_position = find_bytes(data, WATERMARK_START, 0, true)?;
    let end_position = find_bytes(data, WATERMARK_END, start_position, false)?;
    if end_position <= start_position {
        return None;
    }

    let content = if include_content {
        Some(data[start_position + WATERMARK_START.len()..end_position].to_vec())
    } else {
        None
    };

    Some(WatermarkInfo {
        start_position,
        end_position,
        content,
    })
}

fn read_watermark_data(path: &Path) -> Result<Option<(Vec<u8>, u64)>, String> {
    let mut file = OpenOptions::new()
        .read(true)
        .open(path)
        .map_err(|e| format!("Error opening file {}: {e}", path.display()))?;

    let file_size = file
        .seek(SeekFrom::End(0))
        .map_err(|e| format!("Error seeking file {}: {e}", path.display()))?;
    if file_size == 0 {
        return Ok(None);
    }

    let read_len = MAX_WATERMARK_LENGTH.min(file_size as usize);
    file.seek(SeekFrom::Start(file_size - read_len as u64))
        .map_err(|e| format!("Error seeking file tail {}: {e}", path.display()))?;

    let mut tail_data = vec![0_u8; read_len];
    file.read_exact(&mut tail_data)
        .map_err(|e| format!("Error reading file tail {}: {e}", path.display()))?;

    Ok(Some((tail_data, file_size)))
}

pub fn has_watermark(path: &Path) -> Result<bool, String> {
    let Some((tail_data, _)) = read_watermark_data(path)? else {
        return Ok(false);
    };
    Ok(find_watermark(&tail_data, false).is_some())
}

pub fn extract_watermark_text(path: &Path) -> Result<Option<String>, String> {
    let Some((tail_data, _)) = read_watermark_data(path)? else {
        return Ok(None);
    };

    let info = find_watermark(&tail_data, true);
    Ok(info.and_then(|found| found.content).and_then(|bytes| String::from_utf8(bytes).ok()))
}

pub fn add_watermark(path: &Path, encoded_text: &str) -> Result<bool, String> {
    if has_watermark(path)? {
        return Ok(false);
    }

    let mut file = OpenOptions::new()
        .append(true)
        .open(path)
        .map_err(|e| format!("Error opening for append {}: {e}", path.display()))?;

    let mut payload = Vec::with_capacity(WATERMARK_START.len() + encoded_text.len() + WATERMARK_END.len());
    payload.extend_from_slice(WATERMARK_START);
    payload.extend_from_slice(encoded_text.as_bytes());
    payload.extend_from_slice(WATERMARK_END);
    file.write_all(&payload)
        .map_err(|e| format!("Error writing watermark {}: {e}", path.display()))?;
    Ok(true)
}

pub fn remove_watermark(path: &Path) -> Result<bool, String> {
    let Some((tail_data, file_size)) = read_watermark_data(path)? else {
        return Ok(false);
    };
    let Some(wm) = find_watermark(&tail_data, false) else {
        return Ok(false);
    };

    let watermark_position = file_size - (tail_data.len() - wm.start_position) as u64;
    let file = OpenOptions::new()
        .write(true)
        .open(path)
        .map_err(|e| format!("Error opening for truncate {}: {e}", path.display()))?;
    file.set_len(watermark_position)
        .map_err(|e| format!("Error truncating {}: {e}", path.display()))?;
    Ok(true)
}
