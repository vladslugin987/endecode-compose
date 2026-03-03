import { useState } from "react";
import { ActionButton } from "../../components/ui/ActionButton";

type Props = {
  onClear: () => void;
  onFilter: (value: "all" | "info" | "warn" | "error" | "success") => void;
};

export function ConsoleToolbar({ onClear, onFilter }: Props) {
  const [value, setValue] = useState<"all" | "info" | "warn" | "error" | "success">("all");

  return (
    <div className="mb-3 flex items-center justify-between gap-2">
      <div className="flex items-center gap-2">
        <span className="text-sm text-slate-400">Filter:</span>
        <select
          className="rounded border border-slate-700 bg-slate-900 px-2 py-1 text-sm"
          value={value}
          onChange={(e) => {
            const next = e.target.value as typeof value;
            setValue(next);
            onFilter(next);
          }}
        >
          <option value="all">all</option>
          <option value="info">info</option>
          <option value="warn">warn</option>
          <option value="error">error</option>
          <option value="success">success</option>
        </select>
      </div>
      <ActionButton onClick={onClear}>Clear</ActionButton>
    </div>
  );
}
