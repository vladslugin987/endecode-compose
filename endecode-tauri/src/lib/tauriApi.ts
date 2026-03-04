import { invoke } from "@tauri-apps/api/core";
import { openPath } from "@tauri-apps/plugin-opener";

export type EncryptResponse = { job_id: string; total_files: number };
export type DecryptResponse = { job_id: string; total_files: number };
export type BatchResponse = { job_id: string; output_folder: string };
export type AddTextResponse = { job_id: string; matched_files: number; updated_files: number };
export type CancelResponse = { cancelled: boolean };
export type PreviewResponse = {
  image_path: string | null;
  image_data_url: string | null;
  file_count: number;
};

export async function openFolder(folderPath: string) {
  return openPath(folderPath);
}

export async function encryptFolder(payload: { folder_path: string; inject_name: string }) {
  return invoke<EncryptResponse>("encrypt_folder", { payload });
}

export async function decryptFolder(payload: { folder_path: string }) {
  return invoke<DecryptResponse>("decrypt_folder", { payload });
}

export async function batchCopy(payload: {
  source_folder: string;
  num_copies: number;
  base_text: string;
  add_swap: boolean;
  add_watermark: boolean;
  create_zip: boolean;
  watermark_text?: string;
  photo_number?: number;
  visible_size?: "small" | "medium" | "large";
  visible_opacity?: number;
  /** Direct pixel scale 1–24 for visible watermark. Overrides visible_size. */
  visible_scale?: number;
  add_video_watermark?: boolean;
  video_watermark_text?: string;
  video_watermark_timestamp_sec?: number;
  video_watermark_font_size?: number;
}) {
  return invoke<BatchResponse>("batch_copy", { payload });
}

export async function addTextToPhoto(payload: {
  folder_path: string;
  text: string;
  photo_number: number;
  visible_size?: "small" | "medium" | "large";
  visible_opacity?: number;
  /** Direct pixel scale 1–24. Overrides visible_size when provided. */
  visible_scale?: number;
}) {
  return invoke<AddTextResponse>("add_text_to_photo", { payload });
}

export async function cancelJob(payload: { job_id: string }) {
  return invoke<CancelResponse>("cancel_job", { payload });
}

export async function previewFirstImage(payload: { folder_path: string }) {
  return invoke<PreviewResponse>("preview_first_image", { payload });
}

export type UpdateCheckResult = {
  available: boolean;
  version: string | null;
  notes: string | null;
  pub_date: string | null;
};

export async function checkForUpdate() {
  return invoke<UpdateCheckResult>("check_for_update");
}

export async function installUpdate() {
  return invoke<void>("install_update");
}

export type VideoWatermarkResponse = { job_id: string; total_videos: number };

export async function addVideoWatermark(payload: {
  folder_path: string;
  text: string;
  timestamp_sec?: number;
  font_size?: number;
}) {
  return invoke<VideoWatermarkResponse>("add_video_watermark", { payload });
}
