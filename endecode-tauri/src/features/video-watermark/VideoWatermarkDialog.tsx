import { useState } from "react";
import { ActionButton } from "../../components/ui/ActionButton";
import { TextInput } from "../../components/ui/TextInput";

type Props = {
  open: boolean;
  onClose: () => void;
  onConfirm: (text: string, timestampSec: number | undefined, fontSize: number) => void;
};

function CodeLine({ children }: { children: string }) {
  return (
    <code className="block rounded bg-slate-950 px-3 py-1.5 font-mono text-xs text-cyan-300 select-all">
      {children}
    </code>
  );
}

function FfmpegSetupInstructions() {
  return (
    <div className="mt-1 space-y-4 text-xs text-slate-300">
      <p className="text-slate-400">
        This feature requires <strong className="text-slate-200">FFmpeg</strong> to be
        available on your system. The app searches for it automatically in common
        locations — no manual path configuration needed.
      </p>

      {/* macOS */}
      <div>
        <p className="mb-1.5 font-semibold text-slate-200">macOS (recommended: Homebrew)</p>
        <div className="space-y-1">
          <CodeLine>brew install ffmpeg</CodeLine>
        </div>
        <p className="mt-1 text-slate-500">
          If you don't have Homebrew:{" "}
          <span className="text-slate-400">https://brew.sh</span>
        </p>
      </div>

      {/* Windows */}
      <div>
        <p className="mb-1.5 font-semibold text-slate-200">Windows</p>
        <p className="mb-1.5 text-slate-400">
          Download a static build from{" "}
          <span className="text-slate-300">gyan.dev/ffmpeg/builds</span> (choose
          "ffmpeg-release-essentials.zip"), extract it, and either:
        </p>
        <ul className="list-disc space-y-1 pl-4 text-slate-400">
          <li>
            Place <code className="text-cyan-300">ffmpeg.exe</code> and{" "}
            <code className="text-cyan-300">ffprobe.exe</code> in the same folder as
            the app's <code className="text-cyan-300">.exe</code>
          </li>
          <li>
            Or add the <code className="text-cyan-300">bin\</code> folder from the
            archive to your system <code className="text-cyan-300">PATH</code>
          </li>
        </ul>
      </div>

      {/* How the app finds ffmpeg */}
      <div>
        <p className="mb-1.5 font-semibold text-slate-200">Search order</p>
        <ol className="list-decimal space-y-0.5 pl-4 text-slate-400">
          <li>Same folder as the app binary</li>
          <li>App resource directory (bundled sidecar)</li>
          <li>
            macOS Homebrew paths (<code className="text-cyan-300">/opt/homebrew/bin</code>,{" "}
            <code className="text-cyan-300">/usr/local/bin</code>)
          </li>
          <li>System PATH</li>
        </ol>
      </div>

      {/* How the watermark works */}
      <div className="rounded-md border border-slate-700 bg-slate-800/60 p-3">
        <p className="mb-1 font-semibold text-slate-200">How the watermark works</p>
        <p className="text-slate-400">
          FFmpeg splits the video into three segments. Only the middle segment
          (~0.04 s, roughly 2 frames) is re-encoded with a small text overlay
          burned in. The segments before and after are stream-copied — no
          re-encoding, no quality loss. All three parts are then concatenated
          back into a single file that replaces the original.
        </p>
        <p className="mt-1.5 text-slate-500">
          The watermark survives most re-encoding attempts, including fps changes,
          because it is visually embedded into the pixel data of those frames.
        </p>
      </div>
    </div>
  );
}

export function VideoWatermarkDialog({ open, onClose, onConfirm }: Props) {
  const [text, setText] = useState("");
  const [timestampInput, setTimestampInput] = useState("60");
  const [useAutoTs, setUseAutoTs] = useState(false);
  const [fontSize, setFontSize] = useState("16");
  const [showSetup, setShowSetup] = useState(false);

  if (!open) return null;

  function handleConfirm() {
    const trimmed = text.trim();
    if (!trimmed) return;
    const ts = useAutoTs ? undefined : Math.max(0, Number(timestampInput) || 60);
    const fs = Math.min(64, Math.max(8, Number(fontSize) || 16));
    onConfirm(trimmed, ts, fs);
    onClose();
  }

  return (
    <div className="fixed inset-0 z-20 grid place-items-center bg-black/50 p-4">
      <div className="w-full max-w-lg rounded-lg border border-slate-700 bg-slate-900 p-4">
        <div className="mb-3 flex items-start justify-between gap-2">
          <div>
            <h3 className="text-lg font-semibold">Video Watermark</h3>
            <p className="text-xs text-slate-400">
              Burns a tiny visible label into ~0.04 s of footage (stream-copy for the rest).
            </p>
          </div>
          <button
            className="shrink-0 rounded-md border border-slate-700 bg-slate-800 px-2 py-1 text-xs text-slate-300 hover:border-cyan-600 hover:text-cyan-300 transition-colors"
            onClick={() => setShowSetup((v) => !v)}
          >
            {showSetup ? "Hide setup" : "FFmpeg setup ▾"}
          </button>
        </div>

        {showSetup && (
          <div className="mb-4 rounded-md border border-slate-700 bg-slate-800/40 p-3">
            <FfmpegSetupInstructions />
          </div>
        )}

        <div className="grid gap-3">
          <TextInput
            label="Watermark text (e.g. order number)"
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="001"
          />

          <label className="flex items-center gap-2 text-sm text-slate-300 cursor-pointer select-none">
            <input
              type="checkbox"
              className="accent-cyan-500"
              checked={useAutoTs}
              onChange={(e) => setUseAutoTs(e.target.checked)}
            />
            Auto timestamp (60 s or 20 % of duration)
          </label>

          {!useAutoTs && (
            <TextInput
              label="Timestamp (seconds)"
              value={timestampInput}
              onChange={(e) => setTimestampInput(e.target.value)}
              placeholder="60"
            />
          )}

          <TextInput
            label="Font size (8–64)"
            value={fontSize}
            onChange={(e) => setFontSize(e.target.value)}
            placeholder="16"
          />
        </div>

        <div className="mt-4 flex justify-end gap-2">
          <ActionButton onClick={onClose}>Cancel</ActionButton>
          <ActionButton
            variant="primary"
            onClick={handleConfirm}
            disabled={!text.trim()}
          >
            Add Watermark
          </ActionButton>
        </div>
      </div>
    </div>
  );
}
