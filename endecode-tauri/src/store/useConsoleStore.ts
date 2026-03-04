import { create } from "zustand";
import type { ConsoleLog } from "../types";

type ConsoleState = {
  logs: ConsoleLog[];
  pushLog: (log: ConsoleLog) => void;
  clear: () => void;
};

export const useConsoleStore = create<ConsoleState>((set) => ({
  logs: [],
  pushLog: (log) =>
    set((state) => {
      const next = [...state.logs, log];
      return { logs: next.length > 500 ? next.slice(-500) : next };
    }),
  clear: () => set({ logs: [] }),
}));
