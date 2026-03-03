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
    let unlistenLog: (() => void) | undefined;
    let unlistenProgress: (() => void) | undefined;
    let unlistenDone: (() => void) | undefined;

    void (async () => {
      unlistenLog = await listen<{
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

      unlistenProgress = await listen<{
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

      unlistenDone = await listen<{
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
    })();

    return () => {
      unlistenLog?.();
      unlistenProgress?.();
      unlistenDone?.();
    };
  }, [applyProgress, finishJob, pushLog]);
}
