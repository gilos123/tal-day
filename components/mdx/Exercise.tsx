'use client';

import { useState, type ReactNode } from 'react';

export function Exercise({
  n,
  title,
  children,
}: {
  n?: number | string;
  title: string;
  children: ReactNode;
}) {
  return (
    <section className="u-panel p-4 my-5" aria-labelledby={`ex-${n ?? title}`}>
      <header className="flex items-baseline gap-3 mb-2">
        <span className="u-accent-chip">Exercise{n != null ? ` ${n}` : ''}</span>
        <h3 id={`ex-${n ?? title}`} className="text-base font-semibold m-0">
          {title}
        </h3>
      </header>
      <div className="prose-technical !max-w-none [&>*+*]:mt-2 [&>*:first-child]:mt-0">
        {children}
      </div>
    </section>
  );
}

export function Hint({ children }: { children: ReactNode }) {
  return <Reveal label="Show hint" labelOpen="Hide hint">{children}</Reveal>;
}

export function Solution({ children }: { children: ReactNode }) {
  return <Reveal label="Show solution" labelOpen="Hide solution" emphasis>{children}</Reveal>;
}

function Reveal({
  label,
  labelOpen,
  emphasis,
  children,
}: {
  label: string;
  labelOpen: string;
  emphasis?: boolean;
  children: ReactNode;
}) {
  const [open, setOpen] = useState(false);
  return (
    <div className="mt-3">
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        aria-expanded={open}
        className={`text-xs font-mono px-2 py-1 rounded-[var(--radius-sm)] border ${emphasis ? 'border-[var(--color-accent)] text-[var(--color-accent)]' : 'border-[var(--color-border)] text-[var(--color-fg-muted)]'} hover:text-[var(--color-fg)]`}
      >
        {open ? labelOpen : label}
      </button>
      {open ? (
        <div className="mt-2 border-l-2 border-[var(--color-border-strong)] pl-3 [&>*+*]:mt-2">
          {children}
        </div>
      ) : null}
    </div>
  );
}
