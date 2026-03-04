import { useEffect } from "react";
import { useToastStore } from "../store/useToastStore";

const COLORS = {
  info: "bg-slate-800 border-slate-600 text-slate-100",
  success: "bg-emerald-950 border-emerald-700 text-emerald-100",
  error: "bg-rose-950 border-rose-700 text-rose-100",
  warn: "bg-amber-950 border-amber-700 text-amber-200",
};

export function Toast() {
  const message = useToastStore((s) => s.message);
  const type = useToastStore((s) => s.type);
  const hide = useToastStore((s) => s.hide);

  useEffect(() => {
    if (!message) return;
    const t = setTimeout(hide, 4000);
    return () => clearTimeout(t);
  }, [message, hide]);

  if (!message) return null;

  return (
    <div
      className={`fixed bottom-5 right-5 z-50 flex max-w-sm items-start gap-2 rounded-lg border px-4 py-3 text-sm shadow-2xl backdrop-blur-sm ${COLORS[type]}`}
    >
      <span className="flex-1">{message}</span>
      <button
        onClick={hide}
        className="mt-0.5 text-base leading-none opacity-50 hover:opacity-100"
      >
        ×
      </button>
    </div>
  );
}
