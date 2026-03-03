import { create } from "zustand";
import type { JobDone, JobProgress } from "../types";

type OperationState = {
  selectedPath: string;
  nameToInject: string;
  isProcessing: boolean;
  progress: number;
  activeJobId: string | null;
  lastDone: JobDone | null;
  setSelectedPath: (path: string) => void;
  setNameToInject: (name: string) => void;
  startJob: (jobId: string) => void;
  applyProgress: (event: JobProgress) => void;
  finishJob: (event: JobDone) => void;
  resetProgress: () => void;
};

export const useOperationsStore = create<OperationState>((set) => ({
  selectedPath: "",
  nameToInject: "",
  isProcessing: false,
  progress: 0,
  activeJobId: null,
  lastDone: null,
  setSelectedPath: (path) => set({ selectedPath: path }),
  setNameToInject: (name) => set({ nameToInject: name }),
  startJob: (jobId) => set({ isProcessing: true, activeJobId: jobId, progress: 0, lastDone: null }),
  applyProgress: (event) =>
    set((state) => {
      if (!state.activeJobId || state.activeJobId !== event.jobId) return state;
      return { ...state, progress: event.progress };
    }),
  finishJob: (event) =>
    set((state) => {
      if (!state.activeJobId || state.activeJobId !== event.jobId) return state;
      return { ...state, isProcessing: false, progress: 0, activeJobId: null, lastDone: event };
    }),
  resetProgress: () => set({ progress: 0, isProcessing: false, activeJobId: null }),
}));
