import { useEffect, useState } from "react";
import { ActionButton } from "../../components/ui/ActionButton";
import { TextInput } from "../../components/ui/TextInput";
import { WatermarkPreview } from "../preview/WatermarkPreview";

// ─── Persistence ──────────────────────────────────────────────────────────────

const STORAGE_KEY = "endecode_add_text_settings";

type SavedSettings = {
  scale: number;
  opacity: number;
};

function loadSettings(): SavedSettings {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const p = JSON.parse(raw) as Partial<SavedSettings>;
      return {
        scale: typeof p.scale === "number" && p.scale >= 1 ? p.scale : 1,
        opacity: typeof p.opacity === "number" ? Math.min(255, Math.max(30, p.opacity)) : 200,
      };
    }
  } catch {}
  return { scale: 1, opacity: 200 };
}

function saveSettings(s: SavedSettings) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(s));
}

// ─── Component ────────────────────────────────────────────────────────────────

type Props = {
  open: boolean;
  onClose: () => void;
  selectedPath: string;
  onConfirm: (text: string, photoNumber: number, scale: number, opacity: number) => void;
};

export function AddTextDialog({ open, onClose, onConfirm, selectedPath }: Props) {
  const [text, setText] = useState("");
  const [number, setNumber] = useState("");

  // Loaded once from localStorage when dialog first renders.
  const [scale, setScaleRaw] = useState("1");
  const [opacity, setOpacityRaw] = useState("200");
  const [settingsLoaded, setSettingsLoaded] = useState(false);

  useEffect(() => {
    if (!settingsLoaded) {
      const s = loadSettings();
      setScaleRaw(String(s.scale));
      setOpacityRaw(String(s.opacity));
      setSettingsLoaded(true);
    }
  }, [settingsLoaded]);

  if (!open) return null;

  const parsedScale = Math.min(24, Math.max(1, Number(scale) || 1));
  const parsedOpacity = Math.min(255, Math.max(30, Number(opacity) || 200));

  function handleConfirm() {
    const parsedNumber = Number(number);
    if (!text.trim() || !Number.isFinite(parsedNumber)) return;
    saveSettings({ scale: parsedScale, opacity: parsedOpacity });
    onConfirm(text, parsedNumber, parsedScale, parsedOpacity);
    onClose();
  }

  return (
    <div className="fixed inset-0 z-20 grid place-items-center bg-black/50 p-4">
      <div className="w-full max-w-lg rounded-lg border border-slate-700 bg-slate-900 p-4">
        <h3 className="mb-3 text-lg font-semibold">Add Text To Photo</h3>

        <div className="grid gap-3">
          <TextInput
            label="Text to add"
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
          <TextInput
            label="Photo number"
            value={number}
            onChange={(e) => setNumber(e.target.value)}
          />

          {/* Scale control */}
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
                onChange={(e) => setScaleRaw(e.target.value)}
                className="flex-1 accent-cyan-500"
              />
              <input
                type="number"
                min={1}
                max={24}
                value={scale}
                onChange={(e) => setScaleRaw(e.target.value)}
                className="w-16 rounded-md border border-slate-700 bg-slate-800 px-2 py-1 text-center text-sm text-slate-100"
              />
            </div>
            <div className="text-xs text-slate-500">
              Character size: {parsedScale * 8}×{parsedScale * 8} px
            </div>
          </label>

          {/* Opacity control */}
          <label className="flex flex-col gap-1 text-sm">
            <span className="text-slate-300">
              Opacity{" "}
              <span className="text-slate-500">(30–255)</span>
            </span>
            <div className="flex items-center gap-3">
              <input
                type="range"
                min={30}
                max={255}
                step={5}
                value={parsedOpacity}
                onChange={(e) => setOpacityRaw(e.target.value)}
                className="flex-1 accent-cyan-500"
              />
              <input
                type="number"
                min={30}
                max={255}
                value={opacity}
                onChange={(e) => setOpacityRaw(e.target.value)}
                className="w-16 rounded-md border border-slate-700 bg-slate-800 px-2 py-1 text-center text-sm text-slate-100"
              />
            </div>
          </label>

          <WatermarkPreview
            selectedPath={selectedPath}
            text={text}
            scale={parsedScale}
            opacity={parsedOpacity}
          />
        </div>

        <div className="mt-2 rounded-md border border-slate-800 bg-slate-800/50 px-3 py-2 text-xs text-slate-500">
          Settings (scale &amp; opacity) are saved automatically and restored on next launch.
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <ActionButton onClick={onClose}>Cancel</ActionButton>
          <ActionButton
            variant="primary"
            onClick={handleConfirm}
            disabled={!text.trim()}
          >
            Add Text
          </ActionButton>
        </div>
      </div>
    </div>
  );
}
