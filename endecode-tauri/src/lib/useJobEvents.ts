import { useEffect } from "react";
import { listen } from "@tauri-apps/api/event";
import { useConsoleStore } from "../store/useConsoleStore";
import { useOperationsStore } from "../store/useOperationsStore";
import type { JobDone, JobProgress, ConsoleLog } from "../types";

export function useJobEvents() {
  const pushLog = useConsoleStore((state) => state.pushLog);
  const applyProgress = useOperationsStore((state) => state.applyProgress);
  const finishJob = useOperationsStore((state) => state.finishJob);

  useEffect(() => {
    let disposed = false;
    const unlisteners: Array<() => void> = [];

    void (async () => {
      const unlistenLog = await listen<{
        job_id: string;
        level: "info" | "warn" | "error" | "success";
        message: string;
        ts: string;
      }>("job://log", (event) => {
        const payload = event.payload;
        pushLog({
          jobId: payload.job_id,
          level: payload.level,
          message: payload.message,
          ts: payload.ts,
        } satisfies ConsoleLog);
      });
      if (disposed) {
        unlistenLog();
        return;
      }
      unlisteners.push(unlistenLog);

      const unlistenProgress = await listen<{
        job_id: string;
        progress: number;
        current_file?: string;
        stage?: string;
      }>("job://progress", (event) => {
        const payload = event.payload;
        applyProgress({
          jobId: payload.job_id,
          progress: payload.progress,
          currentFile: payload.current_file,
          stage: payload.stage,
        } satisfies JobProgress);
      });
      if (disposed) {
        unlistenProgress();
        return;
      }
      unlisteners.push(unlistenProgress);

      const unlistenDone = await listen<{
        job_id: string;
        status: "ok" | "error" | "cancelled";
        summary?: Record<string, unknown>;
        error?: string;
      }>("job://done", (event) => {
        const payload = event.payload;
        finishJob({
          jobId: payload.job_id,
          status: payload.status,
          summary: payload.summary,
          error: payload.error,
        } satisfies JobDone);
      });
      if (disposed) {
        unlistenDone();
        return;
      }
      unlisteners.push(unlistenDone);
    })();

    return () => {
      disposed = true;
      for (const unlisten of unlisteners) {
        unlisten();
      }
    };
  }, [applyProgress, finishJob, pushLog]);
}
