import { FolderPicker } from "../features/files/FolderPicker";
import { OperationsPanel } from "../features/operations/OperationsPanel";
import { ConsolePanel } from "../features/console/ConsolePanel";
import { useOperationsStore } from "../store/useOperationsStore";
import { useJobEvents } from "../lib/useJobEvents";

export function HomePage() {
  useJobEvents();
  const selectedPath = useOperationsStore((s) => s.selectedPath);
  const setSelectedPath = useOperationsStore((s) => s.setSelectedPath);

  return (
    <div className="mx-auto max-w-7xl p-6">
      <div className="mb-4">
        <h1 className="text-2xl font-bold">ENDEcode Tauri MVP</h1>
        <p className="text-sm text-slate-400">
          1:1 MVP migration: encrypt/decrypt, batch copy, add text, live progress and logs.
        </p>
      </div>

      <div className="grid gap-4 lg:grid-cols-[1fr_1.2fr]">
        <div className="space-y-4">
          <FolderPicker selectedPath={selectedPath} onSelect={setSelectedPath} />
          <OperationsPanel />
        </div>
        <ConsolePanel />
      </div>
    </div>
  );
}
