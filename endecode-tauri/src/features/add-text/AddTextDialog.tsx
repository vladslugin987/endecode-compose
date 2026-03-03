import { useState } from "react";
import { ActionButton } from "../../components/ui/ActionButton";
import { TextInput } from "../../components/ui/TextInput";

type Props = {
  open: boolean;
  onClose: () => void;
  onConfirm: (text: string, photoNumber: number) => void;
};

export function AddTextDialog({ open, onClose, onConfirm }: Props) {
  const [text, setText] = useState("");
  const [number, setNumber] = useState("");

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-20 grid place-items-center bg-black/50 p-4">
      <div className="w-full max-w-lg rounded-lg border border-slate-700 bg-slate-900 p-4">
        <h3 className="mb-3 text-lg font-semibold">Add Text To Photo</h3>
        <div className="grid gap-3">
          <TextInput label="Text to add" value={text} onChange={(e) => setText(e.target.value)} />
          <TextInput label="Photo number" value={number} onChange={(e) => setNumber(e.target.value)} />
        </div>
        <div className="mt-4 flex justify-end gap-2">
          <ActionButton onClick={onClose}>Cancel</ActionButton>
          <ActionButton
            variant="primary"
            onClick={() => {
              const parsed = Number(number);
              if (!text.trim() || !Number.isFinite(parsed)) return;
              onConfirm(text, parsed);
              onClose();
            }}
          >
            Add Text
          </ActionButton>
        </div>
      </div>
    </div>
  );
}
