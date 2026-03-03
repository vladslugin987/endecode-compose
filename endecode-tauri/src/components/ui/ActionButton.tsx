import type { ButtonHTMLAttributes, PropsWithChildren } from "react";

type Variant = "primary" | "secondary" | "danger";

type Props = PropsWithChildren<
  ButtonHTMLAttributes<HTMLButtonElement> & {
    variant?: Variant;
  }
>;

export function ActionButton({ children, variant = "secondary", className = "", ...props }: Props) {
  const map: Record<Variant, string> = {
    primary: "bg-cyan-500 hover:bg-cyan-400 text-slate-900",
    secondary: "bg-slate-700 hover:bg-slate-600 text-slate-100",
    danger: "bg-rose-600 hover:bg-rose-500 text-white",
  };

  return (
    <button
      className={`rounded-md px-4 py-2 font-medium transition disabled:opacity-50 disabled:cursor-not-allowed ${map[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
