import { useEffect, useMemo, useState } from "react";
import { convertFileSrc } from "@tauri-apps/api/core";
import { previewFirstImage } from "../../lib/tauriApi";

type Size = "small" | "medium" | "large";

type Props = {
  selectedPath: string;
  text: string;
  /** Legacy enum-based size. Ignored when `scale` is provided. */
  size?: Size;
  opacity: number;
  /** Direct pixel scale (1–24). Each character will be scale×8 px wide. */
  scale?: number;
};

export function WatermarkPreview({ selectedPath, text, size = "medium", opacity, scale }: Props) {
  const [imagePath, setImagePath] = useState<string>("");
  const [imageDataUrl, setImageDataUrl] = useState<string>("");
  const [error, setError] = useState<string>("");
  const [isHovering, setIsHovering] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setError("");
    if (!selectedPath) {
      setImagePath("");
      setImageDataUrl("");
      return;
    }

    void (async () => {
      try {
        const result = await previewFirstImage({ folder_path: selectedPath });
        if (!cancelled) {
          setImagePath(result.image_path ?? "");
          setImageDataUrl(result.image_data_url ?? "");
        }
      } catch (e) {
        if (!cancelled) {
          setImagePath("");
          setImageDataUrl("");
          setError(`Preview error: ${String(e)}`);
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [selectedPath]);

  // The preview zoom shows ~1/14 of the image at 14× magnification.
  // A character at scale=N is N×8 px in the original.
  // At ~14× zoom the character appears N×8×14 ≈ N×112 px — way too large for CSS.
  // We clamp to a reasonable range so the overlay label looks proportional.
  const fontSize = useMemo(() => {
    if (scale !== undefined) {
      // Each unit of scale = 8 px in original. Preview shows a zoomed region,
      // so we multiply by a small factor to keep text readable at all scales.
      const px = Math.max(7, Math.min(scale * 9, 72));
      return `${px}px`;
    }
    if (size === "small") return "12px";
    if (size === "large") return "18px";
    return "15px";
  }, [scale, size]);

  const source = imageDataUrl || convertFileSrc(imagePath);
  const normalizedOpacity = Math.min(255, Math.max(30, opacity)) / 255;

  if (!selectedPath) {
    return (
      <div className="rounded-md border border-slate-700 p-3 text-xs text-slate-400">
        Select folder to see preview.
      </div>
    );
  }

  if (!imagePath && !imageDataUrl) {
    return (
      <div className="rounded-md border border-slate-700 p-3 text-xs text-slate-400">
        {error || "No image found for preview."}
      </div>
    );
  }

  return (
    <div className="rounded-md border border-slate-700 p-2">
      <div className="mb-2 text-xs text-slate-400">
        Approximate bottom-right watermark preview
        {scale !== undefined && (
          <span className="ml-1 text-slate-500">· scale {scale} ({scale * 8}×{scale * 8} px/char)</span>
        )}
      </div>
      <div
        className="group relative overflow-hidden rounded"
        onMouseEnter={() => setIsHovering(true)}
        onMouseLeave={() => setIsHovering(false)}
      >
        <img src={source} alt="Preview" className="max-h-56 w-full object-cover" />

        {isHovering && (
          <div className="pointer-events-none absolute right-2 top-2 rounded border border-slate-300/60 bg-slate-900/85 p-2">
            <div className="mb-1 text-[10px] text-slate-200">Zoom bottom-right (watermark view)</div>
            <div className="relative h-36 w-36 overflow-hidden rounded border border-slate-500">
              <div
                className="h-full w-full"
                style={{
                  backgroundImage: `url(${source})`,
                  backgroundRepeat: "no-repeat",
                  backgroundPosition: "100% 100%",
                  backgroundSize: "1400% 1400%",
                }}
              />
              <div
                className="absolute bottom-1 right-1 font-semibold text-white"
                style={{
                  opacity: normalizedOpacity,
                  fontSize,
                  textShadow: "0 1px 2px rgba(0,0,0,0.95), 0 0 1px rgba(0,0,0,0.95)",
                  fontFamily: "monospace",
                }}
              >
                {text || "WATERMARK"}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
