import { useState } from "react";
import { ActionButton } from "../../components/ui/ActionButton";
import { TextInput } from "../../components/ui/TextInput";

type BatchPayload = {
  num_copies: number;
  base_text: string;
  add_swap: boolean;
  add_watermark: boolean;
  create_zip: boolean;
  watermark_text?: string;
  photo_number?: number;
};

type Props = {
  open: boolean;
  onClose: () => void;
  onConfirm: (payload: BatchPayload) => void;
};

export function BatchDialog({ open, onClose, onConfirm }: Props) {
  const [copies, setCopies] = useState("1");
  const [baseText, setBaseText] = useState("ORDER");
  const [addSwap, setAddSwap] = useState(false);
  const [addWatermark, setAddWatermark] = useState(false);
  const [createZip, setCreateZip] = useState(false);
  const [watermarkText, setWatermarkText] = useState("");
  const [photoNumber, setPhotoNumber] = useState("");

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-20 grid place-items-center bg-black/50 p-4">
      <div className="w-full max-w-2xl rounded-lg border border-slate-700 bg-slate-900 p-4">
        <h3 className="mb-3 text-lg font-semibold">Batch Copy Settings</h3>
        <div className="grid gap-3 md:grid-cols-2">
          <TextInput label="Number of copies" value={copies} onChange={(e) => setCopies(e.target.value)} />
          <TextInput label="Text base for encoding" value={baseText} onChange={(e) => setBaseText(e.target.value)} />
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={addSwap} onChange={(e) => setAddSwap(e.target.checked)} />Additional swap</label>
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={addWatermark} onChange={(e) => setAddWatermark(e.target.checked)} />Add visible watermark</label>
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={createZip} onChange={(e) => setCreateZip(e.target.checked)} />Create ZIP</label>
          {addWatermark && (
            <>
              <TextInput label="Watermark text (optional)" value={watermarkText} onChange={(e) => setWatermarkText(e.target.value)} />
              <TextInput label="Photo number (optional)" value={photoNumber} onChange={(e) => setPhotoNumber(e.target.value)} />
            </>
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
