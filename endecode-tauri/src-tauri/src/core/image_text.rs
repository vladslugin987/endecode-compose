use std::path::Path;

use font8x8::{BASIC_FONTS, UnicodeFonts};
use image::{ImageBuffer, Rgba};

#[derive(Debug, Clone, Copy)]
pub enum TextPosition {
    TopLeft,
    TopRight,
    Center,
    BottomLeft,
    BottomRight,
}

impl Default for TextPosition {
    fn default() -> Self {
        Self::BottomRight
    }
}

pub fn add_text_to_image(path: &Path, text: &str, position: TextPosition) -> Result<bool, String> {
    let mut img = image::open(path)
        .map_err(|e| format!("Failed to load image {}: {e}", path.display()))?
        .to_rgba8();

    let (img_w, img_h) = img.dimensions();
    let scale = 2_u32;
    let char_w = 8 * scale;
    let char_h = 8 * scale;
    let text_w = (text.chars().count() as u32) * char_w;
    let text_h = char_h;
    let padding = 5_u32;

    let (start_x, start_y) = match position {
        TextPosition::TopLeft => (padding, padding),
        TextPosition::TopRight => (img_w.saturating_sub(text_w + padding), padding),
        TextPosition::Center => (
            img_w.saturating_sub(text_w) / 2,
            img_h.saturating_sub(text_h) / 2,
        ),
        TextPosition::BottomLeft => (padding, img_h.saturating_sub(text_h + padding)),
        TextPosition::BottomRight => (
            img_w.saturating_sub(text_w + padding),
            img_h.saturating_sub(text_h + padding),
        ),
    };

    draw_text_bitmap(&mut img, text, start_x, start_y, scale);
    img.save(path)
        .map_err(|e| format!("Failed to save image {}: {e}", path.display()))?;
    Ok(true)
}

fn draw_text_bitmap(img: &mut ImageBuffer<Rgba<u8>, Vec<u8>>, text: &str, x: u32, y: u32, scale: u32) {
    let mut cursor_x = x;
    for ch in text.chars() {
        if let Some(glyph) = BASIC_FONTS.get(ch) {
            for (row, byte) in glyph.iter().enumerate() {
                for col in 0..8_u32 {
                    if (byte >> col) & 1 == 1 {
                        let px = cursor_x + (7 - col) * scale;
                        let py = y + row as u32 * scale;
                        alpha_square(img, px, py, scale, [255, 255, 255, 120]);
                    }
                }
            }
            cursor_x = cursor_x.saturating_add(8 * scale);
        } else {
            cursor_x = cursor_x.saturating_add(8 * scale);
        }
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
                let blend = |b: u8, f: u8| ((b as f32) * (1.0 - a) + (f as f32) * a).round() as u8;
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
