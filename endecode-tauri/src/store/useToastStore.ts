import { create } from "zustand";

export type ToastType = "info" | "success" | "error" | "warn";

type ToastState = {
  message: string | null;
  type: ToastType;
  show: (message: string, type?: ToastType) => void;
  hide: () => void;
};

export const useToastStore = create<ToastState>((set) => ({
  message: null,
  type: "info",
  show: (message, type = "info") => set({ message, type }),
  hide: () => set({ message: null }),
}));
