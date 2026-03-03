export type LogLevel = "info" | "warn" | "error" | "success";

export type ConsoleLog = {
  jobId: string;
  level: LogLevel;
  message: string;
  ts: string;
};

export type JobProgress = {
  jobId: string;
  progress: number;
  currentFile?: string;
  stage?: string;
};

export type JobDone = {
  jobId: string;
  status: "ok" | "error" | "cancelled";
  summary?: Record<string, unknown>;
  error?: string;
};
