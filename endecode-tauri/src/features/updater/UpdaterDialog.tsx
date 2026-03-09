import { useState } from "react";
import { openPath } from "@tauri-apps/plugin-opener";
import { ActionButton } from "../../components/ui/ActionButton";
import { checkForUpdate, installUpdate, type UpdateCheckResult } from "../../lib/tauriApi";

type Phase = "idle" | "checking" | "found" | "not_found" | "downloading" | "done" | "error";
type Language = "ru" | "en";

type Props = {
  open: boolean;
  onClose: () => void;
};

const RELEASES_URL = "https://github.com/vladslugin987/endecode-compose/releases/latest";

const translations = {
  en: {
    title: "Software Update",
    idle: 'Click "Check for Updates" to look for a new version on GitHub Releases.',
    checking: "Checking GitHub...",
    latest: "You're on the latest version.",
    available: "Update available:",
    released: "Released:",
    downloading: "Downloading & installing...",
    doNotClose: "Do not close the app.",
    installed: "Update installed successfully!",
    reopen:
      "Please quit and reopen the app to start using the new version.",
    closeRestart: "Close & restart manually",
    skip: "Skip",
    downloadInstall: "Download & Install",
    pleaseWait: "Please wait...",
    close: "Close",
    check: "Check for Updates",
    openReleases: "Open Releases Page",
    reinstallTitle: "This update requires a manual reinstall.",
    reinstallDetails:
      "This installed version trusts an older update signing key. Download the latest release from GitHub and install it manually once. After that, future in-app updates should work again.",
    updateFailed: "Update failed.",
  },
  ru: {
    title: "Обновление программы",
    idle: 'Нажмите "Проверить обновления", чтобы найти новую версию в GitHub Releases.',
    checking: "Проверяем GitHub...",
    latest: "У вас уже установлена последняя версия.",
    available: "Доступно обновление:",
    released: "Дата релиза:",
    downloading: "Скачиваем и устанавливаем...",
    doNotClose: "Не закрывайте приложение.",
    installed: "Обновление успешно установлено!",
    reopen:
      "Закройте и снова откройте приложение, чтобы начать работать с новой версией.",
    closeRestart: "Закрыть и перезапустить вручную",
    skip: "Пропустить",
    downloadInstall: "Скачать и установить",
    pleaseWait: "Пожалуйста, подождите...",
    close: "Закрыть",
    check: "Проверить обновления",
    openReleases: "Открыть страницу релизов",
    reinstallTitle: "Для этого обновления нужна ручная переустановка.",
    reinstallDetails:
      "Установленная версия доверяет старому ключу подписи обновлений. Один раз скачайте последнюю версию с GitHub и установите её вручную. После этого встроенные обновления снова будут работать.",
    updateFailed: "Не удалось установить обновление.",
  },
} as const;

function detectLanguage(): Language {
  if (typeof navigator !== "undefined" && navigator.language.toLowerCase().startsWith("ru")) {
    return "ru";
  }
  return "en";
}

function getFriendlyUpdateError(errorMsg: string, language: Language) {
  const normalized = errorMsg.toLowerCase();
  const t = translations[language];

  if (
    normalized.includes("signature was created with a different key than the one provided") ||
    normalized.includes("different key than the one provided")
  ) {
    return {
      title: t.reinstallTitle,
      details: t.reinstallDetails,
      canOpenReleases: true,
    };
  }

  return {
    title: t.updateFailed,
    details: errorMsg,
    canOpenReleases: false,
  };
}

export function UpdaterDialog({ open, onClose }: Props) {
  const [phase, setPhase] = useState<Phase>("idle");
  const [updateInfo, setUpdateInfo] = useState<UpdateCheckResult | null>(null);
  const [errorMsg, setErrorMsg] = useState("");
  const [language, setLanguage] = useState<Language>(detectLanguage);

  if (!open) return null;

  async function handleCheck() {
    setPhase("checking");
    setErrorMsg("");
    try {
      const result = await checkForUpdate();
      setUpdateInfo(result);
      setPhase(result.available ? "found" : "not_found");
    } catch (e) {
      setErrorMsg(String(e));
      setPhase("error");
    }
  }

  async function handleInstall() {
    setPhase("downloading");
    setErrorMsg("");
    try {
      await installUpdate();
      setPhase("done");
    } catch (e) {
      setErrorMsg(String(e));
      setPhase("error");
    }
  }

  const t = translations[language];
  const friendlyError = phase === "error" ? getFriendlyUpdateError(errorMsg, language) : null;

  return (
    <div className="fixed inset-0 z-20 grid place-items-center bg-black/50 p-4">
      <div className="w-full max-w-md rounded-lg border border-slate-700 bg-slate-900 p-5">
        {/* Header */}
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold">{t.title}</h3>
          <div className="flex items-center gap-2">
            <div className="rounded-md border border-slate-700 bg-slate-800 p-0.5 text-xs">
              <button
                className={`rounded px-2 py-1 ${language === "ru" ? "bg-cyan-600 text-white" : "text-slate-400"}`}
                onClick={() => setLanguage("ru")}
              >
                RU
              </button>
              <button
                className={`rounded px-2 py-1 ${language === "en" ? "bg-cyan-600 text-white" : "text-slate-400"}`}
                onClick={() => setLanguage("en")}
              >
                EN
              </button>
            </div>
            <button
              className="text-slate-500 hover:text-slate-300 text-xl leading-none"
              onClick={onClose}
            >
              ×
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="min-h-[80px]">
          {phase === "idle" && (
            <p className="text-sm text-slate-400">
              {t.idle}
            </p>
          )}

          {phase === "checking" && (
            <div className="flex items-center gap-2 text-sm text-slate-400">
              <span className="animate-spin">⟳</span> {t.checking}
            </div>
          )}

          {phase === "not_found" && (
            <p className="text-sm text-green-400">✓ {t.latest}</p>
          )}

          {phase === "found" && updateInfo && (
            <div className="space-y-2">
              <p className="text-sm text-cyan-300 font-medium">
                {t.available} v{updateInfo.version}
              </p>
              {updateInfo.notes && (
                <div className="max-h-40 overflow-y-auto rounded-md border border-slate-700 bg-slate-800 p-3 text-xs text-slate-300 whitespace-pre-wrap">
                  {updateInfo.notes}
                </div>
              )}
              {updateInfo.pub_date && (
                <p className="text-xs text-slate-500">{t.released} {updateInfo.pub_date}</p>
              )}
            </div>
          )}

          {phase === "downloading" && (
            <div className="space-y-2">
              <div className="flex items-center gap-2 text-sm text-slate-400">
                <span className="animate-spin">⟳</span> {t.downloading}
              </div>
              <div className="h-1.5 w-full overflow-hidden rounded bg-slate-700">
                <div className="h-full w-full animate-pulse bg-cyan-500" />
              </div>
              <p className="text-xs text-slate-500">{t.doNotClose}</p>
            </div>
          )}

          {phase === "done" && (
            <div className="space-y-2">
              <p className="text-sm text-green-400">✓ {t.installed}</p>
              <p className="text-xs text-slate-400">{t.reopen}</p>
            </div>
          )}

          {phase === "error" && (
            <div className="space-y-1">
              <p className="text-sm text-red-400">{friendlyError?.title}</p>
              <p className="text-xs text-slate-400">{friendlyError?.details}</p>
              {!friendlyError?.canOpenReleases && (
                <p className="text-xs text-slate-500 break-all">{errorMsg}</p>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="mt-5 flex justify-end gap-2">
          {phase === "done" ? (
            <ActionButton variant="primary" onClick={onClose}>
              {t.closeRestart}
            </ActionButton>
          ) : phase === "found" ? (
            <>
              <ActionButton onClick={onClose}>{t.skip}</ActionButton>
              <ActionButton variant="primary" onClick={handleInstall}>
                {t.downloadInstall}
              </ActionButton>
            </>
          ) : phase === "downloading" || phase === "checking" ? (
            <ActionButton disabled>{t.pleaseWait}</ActionButton>
          ) : phase === "error" && friendlyError?.canOpenReleases ? (
            <>
              <ActionButton onClick={onClose}>{t.close}</ActionButton>
              <ActionButton
                variant="primary"
                onClick={() => {
                  void openPath(RELEASES_URL);
                }}
              >
                {t.openReleases}
              </ActionButton>
            </>
          ) : (
            <>
              <ActionButton onClick={onClose}>{t.close}</ActionButton>
              <ActionButton variant="primary" onClick={handleCheck}>
                {t.check}
              </ActionButton>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
