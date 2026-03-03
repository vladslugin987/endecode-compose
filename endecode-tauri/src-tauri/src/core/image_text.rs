use std::path::Path;

use font8x8::{UnicodeFonts, BASIC_FONTS};
use image::{ImageBuffer, Rgba};

// ─── Public types ─────────────────────────────────────────────────────────────

#[derive(Debug, Clone, Copy)]
pub enum TextPosition {
    TopLeft,
    TopRight,
    Center,
    BottomLeft,
    BottomRight,
}

#[derive(Debug, Clone, Copy)]
pub enum VisibleWatermarkSize {
    Small,
    Medium,
    Large,
}

impl VisibleWatermarkSize {
    pub fn from_optional_str(value: Option<&str>) -> Self {
        match value.unwrap_or("medium").to_ascii_lowercase().as_str() {
            "small" => Self::Small,
            "large" => Self::Large,
            _ => Self::Medium,
        }
    }
}

#[derive(Debug, Clone, Copy)]
pub struct VisibleWatermarkStyle {
    pub size: VisibleWatermarkSize,
    pub opacity: u8,
    /// When set, overrides `size` and uses this exact pixel scale (1–24).
    /// 1 = 8×8 px per character (smallest), 2 = 16×16 px, etc.
    pub scale_override: Option<u32>,
}

impl Default for VisibleWatermarkStyle {
    fn default() -> Self {
        Self { size: VisibleWatermarkSize::Medium, opacity: 200, scale_override: None }
    }
}

impl Default for TextPosition {
    fn default() -> Self {
        Self::BottomRight
    }
}

// ─── Public API ───────────────────────────────────────────────────────────────

pub fn add_text_to_image(path: &Path, text: &str, position: TextPosition) -> Result<bool, String> {
    add_text_to_image_with_style(path, text, position, VisibleWatermarkStyle::default())
}

pub fn add_text_to_image_with_style(
    path: &Path,
    text: &str,
    position: TextPosition,
    style: VisibleWatermarkStyle,
) -> Result<bool, String> {
    let mut img = image::open(path)
        .map_err(|e| format!("Failed to load image {}: {e}", path.display()))?
        .to_rgba8();

    let (img_w, img_h) = img.dimensions();
    let scale = style
        .scale_override
        .map(|s| s.clamp(1, 24))
        .unwrap_or_else(|| compute_adaptive_scale(img_w, img_h, style.size));
    let text_w = (text.chars().count() as u32) * 8 * scale;
    let text_h = 8 * scale;
    let pad = 8_u32; // fixed small padding

    let (x, y) = compute_position(img_w, img_h, text_w, text_h, pad, position);

    // Thin 1-pixel outline so text is legible on any background without a box.
    for dy in -1i32..=1 {
        for dx in -1i32..=1 {
            if dx != 0 || dy != 0 {
                draw_text_bitmap(
                    &mut img,
                    text,
                    (x as i32 + dx).max(0) as u32,
                    (y as i32 + dy).max(0) as u32,
                    scale,
                    [10, 10, 10, 180],
                );
            }
        }
    }
    draw_text_bitmap(&mut img, text, x, y, scale, [255, 255, 255, style.opacity]);

    img.save(path)
        .map_err(|e| format!("Failed to save image {}: {e}", path.display()))?;
    Ok(true)
}

// ─── Scale calculation ────────────────────────────────────────────────────────

/// Pixel scale per glyph cell (font8x8 cell = 8×8 px at scale 1).
/// Small = 1 → each character is literally 8×8 px — barely visible without a magnifier.
pub fn compute_adaptive_scale(_img_w: u32, _img_h: u32, size: VisibleWatermarkSize) -> u32 {
    match size {
        VisibleWatermarkSize::Small => 1,  // 8×8 px per char — magnifier needed
        VisibleWatermarkSize::Medium => 2, // 16×16 px per char
        VisibleWatermarkSize::Large => 4,  // 32×32 px per char
    }
}

// ─── Bitmap renderer ──────────────────────────────────────────────────────────

/// Draw text using font8x8 glyphs.
///
/// font8x8 stores each byte LSB-left: bit 0 = leftmost column, bit 7 = rightmost.
/// `(byte >> col) & 1` with `px = cursor_x + col * scale` renders left-to-right correctly.
fn draw_text_bitmap(
    img: &mut ImageBuffer<Rgba<u8>, Vec<u8>>,
    text: &str,
    x: u32,
    y: u32,
    scale: u32,
    rgba: [u8; 4],
) {
    let mut cursor_x = x;
    for ch in text.chars() {
        if let Some(glyph) = BASIC_FONTS.get(ch).or_else(|| BASIC_FONTS.get('?')) {
            for (row, &byte) in glyph.iter().enumerate() {
                for col in 0..8_u32 {
                    if (byte >> col) & 1 == 1 {
                        let px = cursor_x + col * scale; // LSB-left: col 0 = leftmost
                        let py = y + row as u32 * scale;
                        alpha_square(img, px, py, scale, rgba);
                    }
                }
            }
            cursor_x = cursor_x.saturating_add(8 * scale);
        } else {
            cursor_x = cursor_x.saturating_add(8 * scale);
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

fn compute_position(
    img_w: u32,
    img_h: u32,
    text_w: u32,
    text_h: u32,
    pad: u32,
    position: TextPosition,
) -> (u32, u32) {
    match position {
        TextPosition::TopLeft => (pad, pad),
        TextPosition::TopRight => (img_w.saturating_sub(text_w + pad), pad),
        TextPosition::Center => (
            img_w.saturating_sub(text_w) / 2,
            img_h.saturating_sub(text_h) / 2,
        ),
        TextPosition::BottomLeft => (pad, img_h.saturating_sub(text_h + pad)),
        TextPosition::BottomRight => (
            img_w.saturating_sub(text_w + pad),
            img_h.saturating_sub(text_h + pad),
        ),
    }
}

fn alpha_square(
    img: &mut ImageBuffer<Rgba<u8>, Vec<u8>>,
    x: u32,
    y: u32,
    size: u32,
    rgba: [u8; 4],
) {
    let (w, h) = img.dimensions();
    for dx in 0..size {
        for dy in 0..size {
            let px = x + dx;
            let py = y + dy;
            if px < w && py < h {
                let base = img.get_pixel(px, py).0;
                let a = rgba[3] as f32 / 255.0;
                let blend =
                    |b: u8, f: u8| ((b as f32) * (1.0 - a) + (f as f32) * a).round() as u8;
                img.put_pixel(
                    px,
                    py,
                    Rgba([
                        blend(base[0], rgba[0]),
                        blend(base[1], rgba[1]),
                        blend(base[2], rgba[2]),
                        255,
                    ]),
                );
            }
        }
    }
}

// ─── Tests ────────────────────────────────────────────────────────────────────

#[cfg(test)]
mod tests {
    use super::*;
    use image::{ImageBuffer, Rgba};
    use tempfile::TempDir;

    fn make_test_png(dir: &TempDir, name: &str, width: u32, height: u32) -> std::path::PathBuf {
        let img: ImageBuffer<Rgba<u8>, Vec<u8>> =
            ImageBuffer::from_pixel(width, height, Rgba([50u8, 50, 50, 255]));
        let path = dir.path().join(name);
        img.save(&path).unwrap();
        path
    }

    #[test]
    fn add_text_to_image_valid_png_after_write() {
        let dir = TempDir::new().unwrap();
        let path = make_test_png(&dir, "valid.png", 200, 200);
        add_text_to_image(&path, "HELLO", TextPosition::TopLeft).unwrap();
        image::open(&path).expect("should still be a valid PNG");
    }

    #[test]
    fn add_text_to_image_produces_non_empty_file() {
        let dir = TempDir::new().unwrap();
        let path = make_test_png(&dir, "test.png", 400, 300);
        assert!(add_text_to_image(&path, "TEST", TextPosition::BottomRight).unwrap());
        assert!(std::fs::metadata(&path).unwrap().len() > 0);
    }

    #[test]
    fn add_text_all_positions_without_panic() {
        let dir = TempDir::new().unwrap();
        for (i, pos) in [
            TextPosition::TopLeft,
            TextPosition::TopRight,
            TextPosition::Center,
            TextPosition::BottomLeft,
            TextPosition::BottomRight,
        ]
        .into_iter()
        .enumerate()
        {
            let path = make_test_png(&dir, &format!("pos_{i}.png"), 300, 300);
            add_text_to_image(&path, "OK", pos).unwrap();
        }
    }

    #[test]
    fn add_text_tiny_image_does_not_panic() {
        let dir = TempDir::new().unwrap();
        let path = make_test_png(&dir, "tiny.png", 1, 1);
        let _ = add_text_to_image(&path, "X", TextPosition::Center);
    }

    #[test]
    fn scale_fixed_values() {
        assert_eq!(compute_adaptive_scale(1920, 1080, VisibleWatermarkSize::Small), 1);
        assert_eq!(compute_adaptive_scale(1920, 1080, VisibleWatermarkSize::Medium), 2);
        assert_eq!(compute_adaptive_scale(1920, 1080, VisibleWatermarkSize::Large), 4);
    }

    #[test]
    fn larger_size_enum_gives_larger_scale() {
        let s_small = compute_adaptive_scale(800, 600, VisibleWatermarkSize::Small);
        let s_large = compute_adaptive_scale(800, 600, VisibleWatermarkSize::Large);
        assert!(s_large > s_small);
    }

    #[test]
    fn scale_is_at_least_one() {
        for size in [
            VisibleWatermarkSize::Small,
            VisibleWatermarkSize::Medium,
            VisibleWatermarkSize::Large,
        ] {
            let s = compute_adaptive_scale(1, 1, size);
            assert!(s >= 1, "scale should be >= 1, got {s}");
        }
    }

    #[test]
    fn watermark_size_from_str() {
        assert!(matches!(
            VisibleWatermarkSize::from_optional_str(Some("small")),
            VisibleWatermarkSize::Small
        ));
        assert!(matches!(
            VisibleWatermarkSize::from_optional_str(Some("LARGE")),
            VisibleWatermarkSize::Large
        ));
        assert!(matches!(
            VisibleWatermarkSize::from_optional_str(None),
            VisibleWatermarkSize::Medium
        ));
    }

    /// Verify that text pixels actually appear in the rendered image.
    #[test]
    fn text_pixels_differ_from_background() {
        let dir = TempDir::new().unwrap();
        // Solid grey background
        let path = make_test_png(&dir, "check.png", 400, 400);
        add_text_to_image(&path, "A", TextPosition::TopLeft).unwrap();
        let img = image::open(&path).unwrap().to_rgba8();
        // At least some pixels near the top-left should be lighter than background (50,50,50)
        let changed = (0..80u32)
            .flat_map(|y| (0..80u32).map(move |x| (x, y)))
            .any(|(x, y)| {
                let p = img.get_pixel(x, y).0;
                p[0] > 60 || p[1] > 60 || p[2] > 60
            });
        assert!(changed, "text pixels should be visible on the background");
    }
}
