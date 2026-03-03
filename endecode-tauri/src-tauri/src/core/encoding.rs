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

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::Write;
    use tempfile::NamedTempFile;

    // ── encode / decode round-trips ──────────────────────────────────────────

    #[test]
    fn encode_decode_roundtrip_letters() {
        let original = "HelloWorld";
        assert_eq!(decode_text(&encode_text(original)), original);
    }

    #[test]
    fn encode_decode_roundtrip_digits() {
        let original = "ORDER 001";
        assert_eq!(decode_text(&encode_text(original)), original);
    }

    #[test]
    fn encode_decode_roundtrip_mixed() {
        let original = "Client 42 - Test File ABC xyz";
        assert_eq!(decode_text(&encode_text(original)), original);
    }

    #[test]
    fn encode_preserves_non_alnum() {
        // spaces, dashes, underscores must pass through unchanged
        let original = "hello world - 2025_test!";
        let encoded = encode_text(original);
        // non-alnum chars stay put
        assert!(encoded.contains(' '));
        assert!(encoded.contains('-'));
        assert!(encoded.contains('_'));
        assert!(encoded.contains('!'));
    }

    #[test]
    fn encode_shifts_letters_by_seven() {
        // 'A' → 'H' (shift 7), 'a' → 'h'
        assert_eq!(encode_text("Aa"), "Hh");
    }

    #[test]
    fn encode_wraps_around_alphabet() {
        // 'Z' + 7 wraps: Z=25, 25+7=32 % 26 = 6 → 'G'
        assert_eq!(encode_text("Z"), "G");
        assert_eq!(encode_text("z"), "g");
    }

    #[test]
    fn encode_shifts_digits_by_seven() {
        // '0' → '7', '3' → '0' (wraps)
        assert_eq!(encode_text("03"), "70");
    }

    // ── watermark wrapping ───────────────────────────────────────────────────

    #[test]
    fn add_watermark_contains_markers() {
        let wm = add_watermark("TEST");
        assert!(wm.starts_with(WATERMARK_PREFIX));
        assert!(wm.ends_with(WATERMARK_SUFFIX));
    }

    #[test]
    fn extract_watermark_roundtrip() {
        let text = "ORDER 007";
        let wm = add_watermark(text);
        let full = format!("some file content{wm}");
        let extracted = extract_watermark(&full).expect("should find watermark");
        assert_eq!(decode_text(&extracted), text);
    }

    #[test]
    fn extract_watermark_returns_none_when_absent() {
        assert!(extract_watermark("plain text without markers").is_none());
    }

    #[test]
    fn extract_watermark_picks_last_occurrence() {
        let wm1 = add_watermark("FIRST");
        let wm2 = add_watermark("SECOND");
        let full = format!("data{wm1}more{wm2}end");
        let extracted = extract_watermark(&full).unwrap();
        assert_eq!(decode_text(&extracted), "SECOND");
    }

    // ── process_file ─────────────────────────────────────────────────────────

    #[test]
    fn process_file_appends_watermark() {
        let mut f = NamedTempFile::new().unwrap();
        write!(f, "initial content").unwrap();
        let wm = add_watermark("TEST");
        let result = process_file(f.path(), &wm).unwrap();
        assert!(result, "should return true on first write");
        let content = std::fs::read_to_string(f.path()).unwrap();
        assert!(content.ends_with(&wm));
    }

    #[test]
    fn process_file_skips_if_already_present() {
        let mut f = NamedTempFile::new().unwrap();
        let wm = add_watermark("TEST");
        write!(f, "content{wm}").unwrap();
        let result = process_file(f.path(), &wm).unwrap();
        assert!(!result, "should return false when watermark already present");
    }
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
