mod commands;
mod core;
mod events;
mod state;

use state::AppState;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_updater::Builder::new().build())
        .manage(AppState::default())
        .invoke_handler(tauri::generate_handler![
            commands::encrypt::encrypt_folder,
            commands::decrypt::decrypt_folder,
            commands::batch::batch_copy,
            commands::add_text::add_text_to_photo,
            commands::cancel::cancel_job,
            commands::preview::preview_first_image,
            commands::video_watermark::add_video_watermark,
            commands::updater::check_for_update,
            commands::updater::install_update,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
