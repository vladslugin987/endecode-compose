import { useEffect, useMemo, useRef, useState } from "react";
import { useConsoleStore } from "../../store/useConsoleStore";
import { ConsoleToolbar } from "./ConsoleToolbar";

function formatTs(iso: string): string {
  try {
    // Backend sends unix-ms as a numeric string; frontend system logs send ISO strings.
    const d = /^\d+$/.test(iso.trim()) ? new Date(Number(iso)) : new Date(iso);
    if (isNaN(d.getTime())) return "";
    return d.toLocaleTimeString("en-GB", { hour12: false });
  } catch {
    return "";
  }
}

export function ConsolePanel() {
  const logs = useConsoleStore((s) => s.logs);
  const clear = useConsoleStore((s) => s.clear);
  const [filter, setFilter] = useState<"all" | "info" | "warn" | "error" | "success">("all");
  const scrollRef = useRef<HTMLDivElement>(null);

  const filtered = useMemo(() => {
    if (filter === "all") return logs;
    return logs.filter((x) => x.level === filter);
  }, [filter, logs]);

  // Auto-scroll to bottom on new logs
  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
  }, [filtered]);

  return (
    <div className="flex max-h-[calc(100vh-180px)] min-h-[420px] flex-col rounded-lg border border-slate-800 bg-slate-900/70 p-4">
      <ConsoleToolbar onClear={clear} onFilter={setFilter} />
      <div
        ref={scrollRef}
        className="flex-1 overflow-y-auto rounded border border-slate-800 bg-black/50 p-3 font-mono text-xs"
      >
        {filtered.length === 0 ? (
          <div className="text-slate-500">No logs yet</div>
        ) : (
          filtered.map((log, idx) => (
            <div
              key={`${log.ts}-${idx}`}
              className={`flex gap-2 ${
                log.level === "error"
                  ? "text-rose-400"
                  : log.level === "warn"
                    ? "text-amber-300"
                    : log.level === "success"
                      ? "text-emerald-400"
                      : "text-slate-200"
              }`}
            >
              <span className="shrink-0 text-slate-500">{formatTs(log.ts)}</span>
              <span>
                [{log.level}] {log.message}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
