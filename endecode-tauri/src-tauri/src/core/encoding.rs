use std::{fs, path::Path};

pub const SHIFT: u8 = 7;
pub const WATERMARK_PREFIX: &str = "<<==";
pub const WATERMARK_SUFFIX: &str = "==>>";
pub const OLD_WATERMARK_PREFIX: &str = "*/";

pub fn encode_text(text: &str) -> String {
    text.chars()
        .map(|ch| match ch {
            'A'..='Z' => {
                let idx = ch as u8 - b'A';
                (b'A' + (idx + SHIFT) % 26) as char
            }
            'a'..='z' => {
                let idx = ch as u8 - b'a';
                (b'a' + (idx + SHIFT) % 26) as char
            }
            '0'..='9' => {
                let idx = ch as u8 - b'0';
                char::from(b'0' + (idx + SHIFT) % 10)
            }
            _ => ch,
        })
        .collect()
}

pub fn decode_text(text: &str) -> String {
    text.chars()
        .map(|ch| match ch {
            'A'..='Z' => {
                let idx = ch as i16 - 'A' as i16;
                let shifted = (idx - SHIFT as i16 + 26) % 26;
                (b'A' + shifted as u8) as char
            }
            'a'..='z' => {
                let idx = ch as i16 - 'a' as i16;
                let shifted = (idx - SHIFT as i16 + 26) % 26;
                (b'a' + shifted as u8) as char
            }
            '0'..='9' => {
                let idx = ch as i16 - '0' as i16;
                let shifted = (idx - SHIFT as i16 + 10) % 10;
                char::from(b'0' + shifted as u8)
            }
            _ => ch,
        })
        .collect()
}

pub fn add_watermark(text: &str) -> String {
    format!("{WATERMARK_PREFIX}{}{WATERMARK_SUFFIX}", encode_text(text))
}

pub fn extract_watermark(content: &str) -> Option<String> {
    if let Some(start) = content.rfind(WATERMARK_PREFIX) {
        if let Some(end) = content.rfind(WATERMARK_SUFFIX) {
            let content_start = start + WATERMARK_PREFIX.len();
            if content_start <= end {
                return Some(content[content_start..end].to_string());
            }
        }
    }
    if let Some(start) = content.rfind(OLD_WATERMARK_PREFIX) {
        let content_start = start + OLD_WATERMARK_PREFIX.len();
        return Some(content[content_start..].trim().to_string());
    }
    None
}

pub fn process_file(path: &Path, watermark: &str) -> Result<bool, String> {
    let mut content = fs::read_to_string(path)
        .map_err(|e| format!("Error reading file {}: {e}", path.display()))?;

    if content.contains(watermark) {
        return Ok(false);
    }

    content.push_str(watermark);
    fs::write(path, content).map_err(|e| format!("Error writing file {}: {e}", path.display()))?;
    Ok(true)
}
