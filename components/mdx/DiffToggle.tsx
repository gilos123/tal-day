'use client';

import { useState, type ReactNode } from 'react';

export function DiffToggle({
  full,
  diff,
}: {
  full: ReactNode;
  diff: ReactNode;
}) {
  const [mode, setMode] = useState<'full' | 'diff'>('full');
  return (
    <div>
      <div className="flex items-center gap-2 mb-1">
        <button
          type="button"
          onClick={() => setMode('full')}
          aria-pressed={mode === 'full'}
          className={`text-xs px-2 py-0.5 rounded-[var(--radius-sm)] border ${mode === 'full' ? 'border-[var(--color-accent)] text-[var(--color-accent)]' : 'border-[var(--color-border)] text-[var(--color-fg-muted)]'}`}
        >
          full
        </button>
        <button
          type="button"
          onClick={() => setMode('diff')}
          aria-pressed={mode === 'diff'}
          className={`text-xs px-2 py-0.5 rounded-[var(--radius-sm)] border ${mode === 'diff' ? 'border-[var(--color-accent)] text-[var(--color-accent)]' : 'border-[var(--color-border)] text-[var(--color-fg-muted)]'}`}
        >
          diff from previous
        </button>
      </div>
      <div>{mode === 'full' ? full : diff}</div>
    </div>
  );
}
