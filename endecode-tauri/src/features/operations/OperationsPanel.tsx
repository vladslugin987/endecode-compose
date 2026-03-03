import { useState } from "react";
import { ActionButton } from "../../components/ui/ActionButton";
import { TextInput } from "../../components/ui/TextInput";
import { useOperationsStore } from "../../store/useOperationsStore";
import { useConsoleStore } from "../../store/useConsoleStore";
import { addTextToPhoto, batchCopy, cancelJob, decryptFolder, encryptFolder } from "../../lib/tauriApi";
import { BatchDialog } from "../batch/BatchDialog";
import { AddTextDialog } from "../add-text/AddTextDialog";

export function OperationsPanel() {
  const selectedPath = useOperationsStore((s) => s.selectedPath);
  const nameToInject = useOperationsStore((s) => s.nameToInject);
  const setNameToInject = useOperationsStore((s) => s.setNameToInject);
  const startJob = useOperationsStore((s) => s.startJob);
  const isProcessing = useOperationsStore((s) => s.isProcessing);
  const progress = useOperationsStore((s) => s.progress);
  const activeJobId = useOperationsStore((s) => s.activeJobId);
  const pushLog = useConsoleStore((s) => s.pushLog);

  const [showBatch, setShowBatch] = useState(false);
  const [showAddText, setShowAddText] = useState(false);

  function ensurePath() {
    if (!selectedPath) {
      pushLog({ jobId: "system", level: "error", message: "Error: No folder selected", ts: new Date().toISOString() });
      return false;
    }
    return true;
  }

  return (
    <div className="rounded-lg border border-slate-800 bg-slate-900/70 p-4">
      <h2 className="mb-3 text-lg font-semibold">Actions</h2>

      <div className="grid gap-2 sm:grid-cols-2">
        <ActionButton
          variant="primary"
          disabled={isProcessing}
          onClick={async () => {
            if (!ensurePath()) return;
            if (!nameToInject.trim()) {
              pushLog({ jobId: "system", level: "error", message: "Error: Name to inject is empty", ts: new Date().toISOString() });
              return;
            }
            const result = await encryptFolder({ folder_path: selectedPath, inject_name: nameToInject });
            startJob(result.job_id);
          }}
        >
          ENCRYPT
        </ActionButton>
        <ActionButton
          variant="primary"
          disabled={isProcessing}
          onClick={async () => {
            if (!ensurePath()) return;
            const result = await decryptFolder({ folder_path: selectedPath });
            startJob(result.job_id);
          }}
        >
          DECRYPT
        </ActionButton>
      </div>

      <div className="mt-3">
        <TextInput
          label="Name to inject"
          value={nameToInject}
          onChange={(e) => setNameToInject(e.target.value)}
          placeholder="ORDER 001"
          disabled={isProcessing}
        />
      </div>

      <div className="mt-3 grid gap-2 sm:grid-cols-2">
        <ActionButton disabled={isProcessing} onClick={() => setShowBatch(true)}>
          Batch Copy
        </ActionButton>
        <ActionButton disabled={isProcessing} onClick={() => setShowAddText(true)}>
          Add Text
        </ActionButton>
      </div>

      <div className="mt-3 flex items-center gap-3">
        <div className="h-2 flex-1 overflow-hidden rounded bg-slate-800">
          <div className="h-full bg-cyan-500 transition-all" style={{ width: `${Math.round(progress * 100)}%` }} />
        </div>
        <div className="w-14 text-right text-xs text-slate-400">{Math.round(progress * 100)}%</div>
      </div>

      {activeJobId && (
        <div className="mt-3">
          <ActionButton
            variant="danger"
            onClick={async () => {
              await cancelJob({ job_id: activeJobId });
            }}
          >
            Cancel Active Job
          </ActionButton>
        </div>
      )}

      <BatchDialog
        open={showBatch}
        onClose={() => setShowBatch(false)}
        onConfirm={async (payload) => {
          if (!ensurePath()) return;
          const result = await batchCopy({
            source_folder: selectedPath,
            ...payload,
          });
          startJob(result.job_id);
        }}
      />

      <AddTextDialog
        open={showAddText}
        onClose={() => setShowAddText(false)}
        onConfirm={async (text, photoNumber) => {
          if (!ensurePath()) return;
          const result = await addTextToPhoto({
            folder_path: selectedPath,
            text,
            photo_number: photoNumber,
          });
          startJob(result.job_id);
        }}
      />
    </div>
  );
}
