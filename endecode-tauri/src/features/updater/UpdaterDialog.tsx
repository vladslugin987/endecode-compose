import { useState } from "react";
import { relaunch } from "@tauri-apps/plugin-process";
import { ActionButton } from "../../components/ui/ActionButton";
import { checkForUpdate, installUpdate, type UpdateCheckResult } from "../../lib/tauriApi";

type Phase = "idle" | "checking" | "found" | "not_found" | "downloading" | "done" | "error";

type Props = {
  open: boolean;
  onClose: () => void;
};

export function UpdaterDialog({ open, onClose }: Props) {
  const [phase, setPhase] = useState<Phase>("idle");
  const [updateInfo, setUpdateInfo] = useState<UpdateCheckResult | null>(null);
  const [errorMsg, setErrorMsg] = useState("");

  if (!open) return null;

  async function handleCheck() {
    setPhase("checking");
    setErrorMsg("");
    try {
      const result = await checkForUpdate();
      setUpdateInfo(result);
      setPhase(result.available ? "found" : "not_found");
    } catch (e) {
      setErrorMsg(String(e));
      setPhase("error");
    }
  }

  async function handleInstall() {
    setPhase("downloading");
    setErrorMsg("");
    try {
      await installUpdate();
      setPhase("done");
    } catch (e) {
      setErrorMsg(String(e));
      setPhase("error");
    }
  }

  async function handleRestart() {
    await relaunch();
  }

  return (
    <div className="fixed inset-0 z-20 grid place-items-center bg-black/50 p-4">
      <div className="w-full max-w-md rounded-lg border border-slate-700 bg-slate-900 p-5">
        {/* Header */}
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold">Software Update</h3>
          <button
            className="text-slate-500 hover:text-slate-300 text-xl leading-none"
            onClick={onClose}
          >
            ×
          </button>
        </div>

        {/* Body */}
        <div className="min-h-[80px]">
          {phase === "idle" && (
            <p className="text-sm text-slate-400">
              Click "Check for Updates" to look for a new version on GitHub Releases.
            </p>
          )}

          {phase === "checking" && (
            <div className="flex items-center gap-2 text-sm text-slate-400">
              <span className="animate-spin">⟳</span> Checking GitHub…
            </div>
          )}

          {phase === "not_found" && (
            <p className="text-sm text-green-400">✓ You're on the latest version.</p>
          )}

          {phase === "found" && updateInfo && (
            <div className="space-y-2">
              <p className="text-sm text-cyan-300 font-medium">
                🎉 Update available: v{updateInfo.version}
              </p>
              {updateInfo.notes && (
                <div className="max-h-40 overflow-y-auto rounded-md border border-slate-700 bg-slate-800 p-3 text-xs text-slate-300 whitespace-pre-wrap">
                  {updateInfo.notes}
                </div>
              )}
              {updateInfo.pub_date && (
                <p className="text-xs text-slate-500">Released: {updateInfo.pub_date}</p>
              )}
            </div>
          )}

          {phase === "downloading" && (
            <div className="space-y-2">
              <div className="flex items-center gap-2 text-sm text-slate-400">
                <span className="animate-spin">⟳</span> Downloading &amp; installing…
              </div>
              <div className="h-1.5 w-full overflow-hidden rounded bg-slate-700">
                <div className="h-full w-full animate-pulse bg-cyan-500" />
              </div>
              <p className="text-xs text-slate-500">Do not close the app.</p>
            </div>
          )}

          {phase === "done" && (
            <div className="space-y-2">
              <p className="text-sm text-green-400">✓ Update installed successfully!</p>
              <p className="text-xs text-slate-400">
                Restart the app to start using the new version.
              </p>
            </div>
          )}

          {phase === "error" && (
            <div className="space-y-1">
              <p className="text-sm text-red-400">Update failed.</p>
              <p className="text-xs text-slate-500 break-all">{errorMsg}</p>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="mt-5 flex justify-end gap-2">
          {phase === "done" ? (
            <>
              <ActionButton onClick={onClose}>Later</ActionButton>
              <ActionButton variant="primary" onClick={handleRestart}>
                Restart Now
              </ActionButton>
            </>
          ) : phase === "found" ? (
            <>
              <ActionButton onClick={onClose}>Skip</ActionButton>
              <ActionButton variant="primary" onClick={handleInstall}>
                Download &amp; Install
              </ActionButton>
            </>
          ) : phase === "downloading" || phase === "checking" ? (
            <ActionButton disabled>Please wait…</ActionButton>
          ) : (
            <>
              <ActionButton onClick={onClose}>Close</ActionButton>
              <ActionButton variant="primary" onClick={handleCheck}>
                Check for Updates
              </ActionButton>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
