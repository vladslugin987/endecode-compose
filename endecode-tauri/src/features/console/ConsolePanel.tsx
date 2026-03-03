import { useMemo, useState } from "react";
import { useConsoleStore } from "../../store/useConsoleStore";
import { ConsoleToolbar } from "./ConsoleToolbar";

export function ConsolePanel() {
  const logs = useConsoleStore((s) => s.logs);
  const clear = useConsoleStore((s) => s.clear);
  const [filter, setFilter] = useState<"all" | "info" | "warn" | "error" | "success">("all");

  const filtered = useMemo(() => {
    if (filter === "all") return logs;
    return logs.filter((x) => x.level === filter);
  }, [filter, logs]);

  return (
    <div className="flex h-full min-h-[420px] flex-col rounded-lg border border-slate-800 bg-slate-900/70 p-4">
      <ConsoleToolbar onClear={clear} onFilter={setFilter} />
      <div className="flex-1 overflow-auto rounded border border-slate-800 bg-black/50 p-3 font-mono text-xs">
        {filtered.length === 0 ? (
          <div className="text-slate-500">No logs yet</div>
        ) : (
          filtered.map((log, idx) => (
            <div
              key={`${log.ts}-${idx}`}
              className={
                log.level === "error"
                  ? "text-rose-400"
                  : log.level === "warn"
                    ? "text-amber-300"
                    : log.level === "success"
                      ? "text-emerald-400"
                      : "text-slate-200"
              }
            >
              [{log.level}] {log.message}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
