import { useEffect } from "react";
import { FolderPicker } from "../features/files/FolderPicker";
import { OperationsPanel } from "../features/operations/OperationsPanel";
import { ConsolePanel } from "../features/console/ConsolePanel";
import { Toast } from "../components/Toast";
import { useOperationsStore } from "../store/useOperationsStore";
import { useToastStore } from "../store/useToastStore";
import { useJobEvents } from "../lib/useJobEvents";

export function HomePage() {
  useJobEvents();
  const selectedPath = useOperationsStore((s) => s.selectedPath);
  const setSelectedPath = useOperationsStore((s) => s.setSelectedPath);
  const lastDone = useOperationsStore((s) => s.lastDone);
  const showToast = useToastStore((s) => s.show);

  // Toast when job finishes
  useEffect(() => {
    if (!lastDone) return;
    if (lastDone.status === "ok") {
      showToast("✓ Job completed successfully", "success");
    } else if (lastDone.status === "error") {
      showToast(`✗ ${lastDone.error ?? "Job failed"}`, "error");
    } else if (lastDone.status === "cancelled") {
      showToast("⚠ Job cancelled", "warn");
    }
  }, [lastDone, showToast]);

  return (
    <div className="mx-auto max-w-7xl p-6">
      <div className="mb-4">
        <h1 className="text-2xl font-bold">ENDEcode</h1>
        <p className="text-sm text-slate-400">
          Encrypt · Decrypt · Batch copy · Watermark · Live logs
        </p>
      </div>

      <div className="grid gap-4 lg:grid-cols-[1fr_1.2fr] lg:items-start">
        <div className="space-y-4">
          <FolderPicker selectedPath={selectedPath} onSelect={setSelectedPath} />
          <OperationsPanel />
        </div>
        <ConsolePanel />
      </div>

      <Toast />
    </div>
  );
}
