import type { ReactNode } from 'react';

type Variant = 'note' | 'warning' | 'hard' | 'tip';

const labels: Record<Variant, string> = {
  note: 'Note',
  warning: 'Warning',
  hard: 'This is hard',
  tip: 'Tip',
};

const accent: Record<Variant, string> = {
  note: 'var(--color-border-strong)',
  warning: 'var(--color-warn)',
  hard: 'var(--color-accent)',
  tip: 'var(--color-ok)',
};

const icon: Record<Variant, string> = {
  note: '●',
  warning: '▲',
  hard: '※',
  tip: '→',
};

export function Aside({
  variant = 'note',
  title,
  children,
}: {
  variant?: Variant;
  title?: string;
  children: ReactNode;
}) {
  const color = accent[variant];
  return (
    <aside
      role="note"
      className="not-prose my-5 u-panel overflow-hidden"
      aria-label={title ?? labels[variant]}
      style={{
        borderLeft: `3px solid ${color}`,
        background: `linear-gradient(90deg, color-mix(in oklab, ${color} 6%, var(--color-surface)), var(--color-surface) 40%)`,
      }}
    >
      <div className="px-4 pt-3 pb-2 flex items-center gap-2">
        <span
          className="inline-flex items-center justify-center w-4 h-4 text-[11px] leading-none"
          style={{ color }}
          aria-hidden
        >
          {icon[variant]}
        </span>
        <div
          className="text-[10px] uppercase tracking-[0.14em] font-mono font-semibold"
          style={{ color }}
        >
          {title ?? labels[variant]}
        </div>
      </div>
      <div className="prose-technical !max-w-none px-4 pb-3 [&>*+*]:mt-2 [&>*:first-child]:mt-0">
        {children}
      </div>
    </aside>
  );
}
