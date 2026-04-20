'use client';

import { useEffect, useState } from 'react';
import { ensureComplete } from '@/lib/progress';
import {
  isCheckedToday,
  checkToday,
  uncheckToday,
  subscribe,
} from '@/lib/daily-checks';

/**
 * Button is driven by the "ticked today" flag from `lib/daily-checks.ts`, which
 * auto-resets at the start of each UTC day. When the user ticks for the first
 * time, we also record a lifetime completion via `ensureComplete` so the
 * Progress Dashboard keeps reflecting the lesson as ever-completed; subsequent
 * un-ticks within the same day only clear the daily flag, never the lifetime
 * record — so progress is never lost.
 */
export function MarkComplete({ slug }: { slug: string }) {
  const [done, setDone] = useState(false);
  useEffect(() => {
    const update = () => setDone(isCheckedToday(slug));
    update();
    return subscribe(update);
  }, [slug]);

  const handleClick = () => {
    if (done) {
      uncheckToday(slug);
    } else {
      checkToday(slug);
      ensureComplete(slug);
    }
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      aria-pressed={done}
      className={`text-xs font-mono px-3 py-1.5 rounded-[var(--radius-sm)] border ${
        done
          ? 'border-[var(--color-accent)] text-[var(--color-accent-fg)] bg-[var(--color-accent)]'
          : 'border-[var(--color-border-strong)] text-[var(--color-fg-muted)] hover:text-[var(--color-fg)]'
      }`}
    >
      {done ? '✓ completed' : 'mark as complete'}
    </button>
  );
}
