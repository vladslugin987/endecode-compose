import { open } from "@tauri-apps/plugin-dialog";
import { ActionButton } from "../../components/ui/ActionButton";

type Props = {
  selectedPath: string;
  onSelect: (path: string) => void;
};

export function FolderPicker({ selectedPath, onSelect }: Props) {
  async function pickFolder() {
    const result = await open({ directory: true, multiple: false });
    if (typeof result === "string") {
      onSelect(result);
    }
  }

  return (
    <div className="rounded-lg border border-slate-800 bg-slate-900/70 p-4">
      <div className="flex items-center justify-between gap-3">
        <div className="min-w-0">
          <div className="text-sm text-slate-400">Selected folder</div>
          <div className="truncate text-slate-100">{selectedPath || "Not selected"}</div>
        </div>
        <ActionButton variant="primary" onClick={pickFolder}>
          Choose folder
        </ActionButton>
      </div>
    </div>
  );
}
