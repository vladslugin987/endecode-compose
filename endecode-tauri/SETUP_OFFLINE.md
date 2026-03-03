# Offline Rust Setup (Corporate Network)

This project can stay in the **current repository**.  
You do **not** need to create a new repository for vendor/offline support.

## Why this is needed

Some corporate proxies block direct downloads from `crates.io` / `static.crates.io`, which breaks Tauri Rust dependency fetch.

## CI-based vendor workflow

Use GitHub Actions workflow:

- `.github/workflows/vendor-refresh.yml`

It creates an artifact named:

- `endecode-tauri-rust-vendor`

The artifact contains:

- `vendor/`
- `.cargo/config.toml` (with source replacement to vendored crates)
- `Cargo.lock`
- `Cargo.toml`

## How to use the artifact locally

1. Run workflow manually from GitHub Actions (`workflow_dispatch`).
2. Download artifact `endecode-tauri-rust-vendor`.
3. Extract artifact contents into:
   - `endecode-tauri/src-tauri/`
4. Ensure these paths exist:
   - `endecode-tauri/src-tauri/vendor/`
   - `endecode-tauri/src-tauri/.cargo/config.toml`
5. Run:
   - `cd endecode-tauri`
   - `npm run tauri dev`

## Notes

- Existing local file `src-tauri/.cargo/config.toml` with `check-revoke = false` helps with Windows Schannel revocation checks.
- Vendored `.cargo/config.toml` from artifact is the one Cargo uses for offline source replacement.
- If dependencies change in `Cargo.toml`, run `vendor-refresh` again and replace local vendor bundle.
