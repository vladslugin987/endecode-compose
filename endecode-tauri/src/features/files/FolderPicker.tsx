import { useState } from "react";
import { open } from "@tauri-apps/plugin-dialog";
import { ActionButton } from "../../components/ui/ActionButton";
import { previewFirstImage } from "../../lib/tauriApi";

type Props = {
  selectedPath: string;
  onSelect: (path: string) => void;
};

export function FolderPicker({ selectedPath, onSelect }: Props) {
  const [fileCount, setFileCount] = useState<number | null>(null);

  async function pickFolder() {
    try {
      const result = await open({
        directory: true,
        multiple: false,
        title: "Select folder",
      });

      if (typeof result === "string") {
        onSelect(result);
        // Fetch file count in background
        try {
          const preview = await previewFirstImage({ folder_path: result });
          setFileCount(preview.file_count);
        } catch {
          setFileCount(null);
        }
      }
    } catch (error) {
      console.error("Failed to open folder picker:", error);
    }
  }

  return (
    <div className="rounded-lg border border-slate-800 bg-slate-900/70 p-4">
      <div className="flex items-center justify-between gap-3">
        <div className="min-w-0">
          <div className="flex items-center gap-2 text-sm text-slate-400">
            <span>Selected folder</span>
            {selectedPath && fileCount !== null && (
              <span className="rounded-full bg-slate-700 px-2 py-0.5 text-xs text-slate-300">
                {fileCount} {fileCount === 1 ? "file" : "files"}
              </span>
            )}
          </div>
          <div className="truncate text-slate-100">{selectedPath || "Not selected"}</div>
        </div>
        <ActionButton variant="primary" onClick={pickFolder}>
          Choose folder
        </ActionButton>
      </div>
    </div>
  );
}
