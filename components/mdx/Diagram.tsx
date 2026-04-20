import type { ReactNode } from 'react';

export function Diagram({
  alt,
  caption,
  width,
  height,
  children,
}: {
  alt: string;
  caption?: string;
  width?: number | string;
  height?: number | string;
  children: ReactNode;
}) {
  if (!alt) {
    throw new Error('<Diagram> requires a non-empty `alt` for accessibility.');
  }
  return (
    <figure className="not-prose my-6">
      <div
        role="img"
        aria-label={alt}
        className="u-panel overflow-auto p-4 flex justify-center"
        style={{ width: width ? `${typeof width === 'number' ? width + 'px' : width}` : undefined }}
      >
        <div style={{ maxWidth: width, maxHeight: height }}>{children}</div>
      </div>
      {caption ? (
        <figcaption className="text-xs text-[var(--color-fg-muted)] mt-2 max-w-[72ch]">
          {caption}
        </figcaption>
      ) : null}
      <div className="sr-only">{alt}</div>
    </figure>
  );
}
