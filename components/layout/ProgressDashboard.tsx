'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { getAllCompleted, subscribe } from '@/lib/progress';

type LessonSummary = { slug: string; title: string; order: number; status: 'full' | 'stub' };

export function ProgressDashboard({ lessons }: { lessons: LessonSummary[] }) {
  const [completed, setCompleted] = useState<string[]>([]);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const update = () => setCompleted(getAllCompleted());
    update();
    return subscribe(update);
  }, []);

  const total = lessons.length;
  const doneCount = completed.length;
  const pct = total ? Math.round((doneCount / total) * 100) : 0;

  return (
    <section aria-label="Progress" className="u-panel p-4">
      <div className="flex items-baseline justify-between mb-3">
        <h2 className="text-sm uppercase tracking-wider text-[var(--color-fg-muted)] m-0">
          Your progress
        </h2>
        {mounted ? (
          <span className="font-mono text-xs text-[var(--color-fg-muted)]">
            {doneCount} / {total}
          </span>
        ) : null}
      </div>
      <div
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={total}
        aria-valuenow={mounted ? doneCount : 0}
        className="h-1 bg-[var(--color-surface-2)] rounded-full overflow-hidden"
      >
        <div
          style={{ width: `${pct}%` }}
          className="h-full bg-[var(--color-accent)] transition-[width]"
        />
      </div>
      <ul className="mt-4 grid gap-1 font-mono text-sm">
        {lessons.map((l) => {
          const isDone = mounted && completed.includes(l.slug);
          return (
            <li key={l.slug} className="flex items-center gap-3">
              <span
                aria-hidden
                className="inline-block"
                style={{
                  width: 10,
                  height: 10,
                  borderRadius: 999,
                  background: isDone ? 'var(--color-accent)' : 'transparent',
                  border: isDone ? 'none' : '1px solid var(--color-border-strong)',
                }}
              />
              <Link
                href={`/lessons/${l.slug}`}
                className="text-[var(--color-fg)] hover:text-[var(--color-accent)]"
              >
                <span className="text-[var(--color-fg-dim)]">
                  {String(l.order).padStart(2, '0')}
                </span>{' '}
                {l.title}
                {l.status === 'stub' ? (
                  <span className="ml-1 text-[var(--color-fg-dim)]">(stub)</span>
                ) : null}
              </Link>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
