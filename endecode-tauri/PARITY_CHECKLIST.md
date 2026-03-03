# MVP Parity Checklist (Compose vs Tauri)

## Scope

- Encrypt folder
- Decrypt folder
- Batch copy and encode
- Add text to photo
- Job progress/log/done events

## Results

| Scenario | Compose reference | Tauri status | Notes |
|---|---|---|---|
| Caesar encode/decode (`SHIFT=7`) | `EncodingUtils.kt` | Done | Same shift and char classes |
| Watermark marker format `<<==...==>>` | `EncodingUtils.kt` + `WatermarkUtils.kt` | Done | Same prefix/suffix and extraction model |
| Append watermark to files | `EncodingUtils.processFile` | Done | Same behavior, UTF-8 read/write |
| Byte-tail watermark for videos | `WatermarkUtils.addWatermark` | Done | Added in `watermark.rs` |
| Decrypt flow with fallback logs | `HomeViewModel.decrypt` | Done | New-format decode + partial/no-watermark messages |
| Batch copy pipeline | `BatchUtils.performBatchCopyAndEncode` | Done | Copy -> encode -> optional watermark/swap/zip |
| Add visible text to photo | `ImageUtils.addTextToImage` | Done (functional) | Bitmap text renderer instead of OpenCV |
| Async progress and logs | `HomeViewModel` + `ConsoleState` | Done | `job://progress`, `job://log`, `job://done` |
| Cancel operation | N/A in Compose MVP | Added | `cancel_job` command |

## Known deviations

1. **Image rendering engine differs**: Compose uses OpenCV text render, Tauri MVP uses bitmap font rendering (`font8x8`) for portability.
2. **Timestamp format differs**: event `ts` is debug-formatted `SystemTime`.
3. **Rust compile check blocked in this environment**: local cargo registry TLS revocation check failed during dependency fetch.

## Manual smoke steps

1. Open `endecode-tauri`.
2. Run `npm install`.
3. Run `npm run tauri dev`.
4. Pick a folder with mixed files (`txt/jpg/mp4`).
5. Test:
   - Encrypt with `ORDER 001`
   - Decrypt and verify logs show decoded text
   - Batch copy with 2 copies + optional zip
   - Add text to photo by number
6. Confirm progress bar updates and logs stream in console.
