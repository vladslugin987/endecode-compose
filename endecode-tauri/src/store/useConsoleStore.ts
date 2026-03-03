import { create } from "zustand";
import type { ConsoleLog } from "../types";

type ConsoleState = {
  logs: ConsoleLog[];
  pushLog: (log: ConsoleLog) => void;
  clear: () => void;
};

export const useConsoleStore = create<ConsoleState>((set) => ({
  logs: [],
  pushLog: (log) => set((state) => ({ logs: [...state.logs, log] })),
  clear: () => set({ logs: [] }),
}));
