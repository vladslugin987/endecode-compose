import { useState } from "react";
import { ActionButton } from "../../components/ui/ActionButton";
import { TextInput } from "../../components/ui/TextInput";
import { WatermarkPreview } from "../preview/WatermarkPreview";

type BatchPayload = {
  num_copies: number;
  base_text: string;
  add_swap: boolean;
  add_watermark: boolean;
  create_zip: boolean;
  watermark_text?: string;
  photo_number?: number;
  visible_scale?: number;
  visible_opacity?: number;
  add_video_watermark?: boolean;
  video_watermark_text?: string;
  video_watermark_timestamp_sec?: number;
  video_watermark_font_size?: number;
};

type Props = {
  open: boolean;
  onClose: () => void;
  selectedPath: string;
  onConfirm: (payload: BatchPayload) => void;
};

export function BatchDialog({ open, onClose, onConfirm, selectedPath }: Props) {
  const [copies, setCopies] = useState("1");
  const [baseText, setBaseText] = useState("ORDER");
  const [addSwap, setAddSwap] = useState(false);
  const [addWatermark, setAddWatermark] = useState(false);
  const [createZip, setCreateZip] = useState(false);
  const [watermarkText, setWatermarkText] = useState("");
  const [photoNumber, setPhotoNumber] = useState("");

  // Visible watermark scale (1 = 8×8 px/char — barely visible)
  const [wScale, setWScaleRaw] = useState("1");
  const [wOpacity, setWOpacityRaw] = useState("180");

  // Video watermark
  const [addVideoWm, setAddVideoWm] = useState(false);
  const [videoWmText, setVideoWmText] = useState("");
  const [videoWmTs, setVideoWmTs] = useState("60");
  const [videoWmFs, setVideoWmFs] = useState("12");

  if (!open) return null;

  const parsedScale = Math.min(24, Math.max(1, Number(wScale) || 1));
  const parsedOpacity = Math.min(255, Math.max(30, Number(wOpacity) || 180));

  return (
    <div className="fixed inset-0 z-20 grid place-items-center bg-black/50 p-4">
      <div className="w-full max-w-2xl rounded-lg border border-slate-700 bg-slate-900 p-4">
        <h3 className="mb-3 text-lg font-semibold">Batch Copy Settings</h3>

        <div className="grid gap-3 md:grid-cols-2">
          <TextInput label="Number of copies" value={copies} onChange={(e) => setCopies(e.target.value)} />
          <TextInput label="Text base for encoding" value={baseText} onChange={(e) => setBaseText(e.target.value)} />

          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={addSwap} onChange={(e) => setAddSwap(e.target.checked)} />
            Additional swap
          </label>
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={createZip} onChange={(e) => setCreateZip(e.target.checked)} />
            Create ZIP
          </label>

          {/* ── Visible photo watermark ── */}
          <label className="flex items-center gap-2 text-sm md:col-span-2">
            <input type="checkbox" checked={addWatermark} onChange={(e) => setAddWatermark(e.target.checked)} />
            Add visible watermark to photo
          </label>

          {addWatermark && (
            <div className="md:col-span-2 grid gap-3 rounded-md border border-slate-700 bg-slate-800/40 p-3 md:grid-cols-2">
              <TextInput label="Watermark text (optional, default = order number)" value={watermarkText} onChange={(e) => setWatermarkText(e.target.value)} />
              <TextInput label="Photo number (optional)" value={photoNumber} onChange={(e) => setPhotoNumber(e.target.value)} />

              {/* Scale */}
              <label className="flex flex-col gap-1 text-sm">
                <span className="text-slate-300">
                  Pixel scale{" "}
                  <span className="text-slate-500">(1 = 8×8 px/char — barely visible)</span>
                </span>
                <div className="flex items-center gap-3">
                  <input
                    type="range"
                    min={1}
                    max={24}
                    step={1}
                    value={parsedScale}
                    onChange={(e) => setWScaleRaw(e.target.value)}
                    className="flex-1 accent-cyan-500"
                  />
                  <input
                    type="number"
                    min={1}
                    max={24}
                    value={wScale}
                    onChange={(e) => setWScaleRaw(e.target.value)}
                    className="w-16 rounded-md border border-slate-700 bg-slate-800 px-2 py-1 text-center text-sm"
                  />
                </div>
                <div className="text-xs text-slate-500">
                  Character size: {parsedScale * 8}×{parsedScale * 8} px
                </div>
              </label>

              {/* Opacity */}
              <label className="flex flex-col gap-1 text-sm">
                <span className="text-slate-300">Opacity (30–255)</span>
                <div className="flex items-center gap-3">
                  <input
                    type="range"
                    min={30}
                    max={255}
                    step={5}
                    value={parsedOpacity}
                    onChange={(e) => setWOpacityRaw(e.target.value)}
                    className="flex-1 accent-cyan-500"
                  />
                  <input
                    type="number"
                    min={30}
                    max={255}
                    value={wOpacity}
                    onChange={(e) => setWOpacityRaw(e.target.value)}
                    className="w-16 rounded-md border border-slate-700 bg-slate-800 px-2 py-1 text-center text-sm"
                  />
                </div>
              </label>

              <div className="md:col-span-2">
                <WatermarkPreview
                  selectedPath={selectedPath}
                  text={watermarkText || "001"}
                  scale={parsedScale}
                  opacity={parsedOpacity}
                />
              </div>
            </div>
          )}

          {/* ── Video watermark ── */}
          <label className="flex items-center gap-2 text-sm md:col-span-2">
            <input type="checkbox" checked={addVideoWm} onChange={(e) => setAddVideoWm(e.target.checked)} />
            Add visible watermark to video (requires FFmpeg)
          </label>

          {addVideoWm && (
            <div className="md:col-span-2 grid gap-3 rounded-md border border-slate-700 bg-slate-800/40 p-3 md:grid-cols-3">
              <TextInput
                label="Video watermark text (default = order number)"
                value={videoWmText}
                onChange={(e) => setVideoWmText(e.target.value)}
              />
              <TextInput
                label="Timestamp (sec, default 60)"
                value={videoWmTs}
                onChange={(e) => setVideoWmTs(e.target.value)}
              />
              <TextInput
                label="Font size (default 12)"
                value={videoWmFs}
                onChange={(e) => setVideoWmFs(e.target.value)}
              />
              <p className="md:col-span-3 text-xs text-slate-500">
                Burns ~0.04 s of a tiny text into the video at the given timestamp. FFmpeg must be installed (macOS: brew install ffmpeg; Windows: add ffmpeg.exe next to the app).
              </p>
            </div>
          )}
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <ActionButton onClick={onClose}>Cancel</ActionButton>
          <ActionButton
            variant="primary"
            onClick={() => {
              const num = Number(copies);
              if (!Number.isFinite(num) || num <= 0 || !baseText.trim()) return;
              onConfirm({
                num_copies: num,
                base_text: baseText,
                add_swap: addSwap,
                add_watermark: addWatermark,
                create_zip: createZip,
                watermark_text: watermarkText.trim() || undefined,
                photo_number: photoNumber.trim() ? Number(photoNumber) : undefined,
                visible_scale: addWatermark ? parsedScale : undefined,
                visible_opacity: addWatermark ? parsedOpacity : undefined,
                add_video_watermark: addVideoWm || undefined,
                video_watermark_text: addVideoWm && videoWmText.trim() ? videoWmText.trim() : undefined,
                video_watermark_timestamp_sec: addVideoWm && videoWmTs.trim() ? Number(videoWmTs) : undefined,
                video_watermark_font_size: addVideoWm && videoWmFs.trim() ? Number(videoWmFs) : undefined,
              });
              onClose();
            }}
          >
            Start
          </ActionButton>
        </div>
      </div>
    </div>
  );
}
