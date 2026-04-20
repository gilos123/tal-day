'use client';

import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import { loadSearchIndex, type SearchDoc } from '@/lib/search-index';

type Hit = SearchDoc & { score: number };

export function SearchModal() {
  const [open, setOpen] = useState(false);
  const [q, setQ] = useState('');
  const [hits, setHits] = useState<Hit[]>([]);
  const [error, setError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === '/' && !open) {
        const t = e.target as HTMLElement | null;
        if (
          t &&
          (t.tagName === 'INPUT' ||
            t.tagName === 'TEXTAREA' ||
            t.isContentEditable)
        ) {
          return;
        }
        e.preventDefault();
        setOpen(true);
      } else if (e.key === 'Escape' && open) {
        setOpen(false);
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open]);

  useEffect(() => {
    if (!open) return;
    setTimeout(() => inputRef.current?.focus(), 10);
  }, [open]);

  useEffect(() => {
    if (!q.trim()) {
      setHits([]);
      return;
    }
    let cancelled = false;
    loadSearchIndex()
      .then((mini) => {
        if (cancelled) return;
        const raw = mini.search(q, { prefix: true, fuzzy: 0.15 }).slice(0, 20);
        setHits(raw as unknown as Hit[]);
      })
      .catch((e) => {
        setError(String(e?.message ?? e));
      });
    return () => {
      cancelled = true;
    };
  }, [q]);

  if (!open) return null;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Search lessons"
      className="fixed inset-0 z-50 flex items-start justify-center pt-20 px-4"
      onClick={() => setOpen(false)}
    >
      <div className="fixed inset-0 bg-black/60" aria-hidden />
      <div
        onClick={(e) => e.stopPropagation()}
        className="relative u-panel w-full max-w-xl p-2 shadow-[var(--shadow-panel)]"
      >
        <input
          ref={inputRef}
          type="search"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Search lessons…"
          aria-label="Search query"
          className="w-full bg-transparent border-0 outline-none px-3 py-2 text-base font-mono placeholder:text-[var(--color-fg-dim)]"
        />
        {error ? (
          <div className="px-3 py-2 text-xs text-[var(--color-danger)]">{error}</div>
        ) : null}
        {hits.length > 0 ? (
          <ul className="mt-1 max-h-[60vh] overflow-auto border-t border-[var(--color-border)] divide-y divide-[var(--color-border)]">
            {hits.map((h) => (
              <li key={h.id}>
                <Link
                  href={`/lessons/${h.slug}${h.anchor ? `#${h.anchor}` : ''}`}
                  onClick={() => setOpen(false)}
                  className="block px-3 py-2 hover:bg-[var(--color-surface-2)]"
                >
                  <div className="text-sm font-mono text-[var(--color-accent)]">
                    {h.lessonTitle}
                    {h.heading && h.heading !== h.lessonTitle ? (
                      <span className="text-[var(--color-fg-muted)]"> → {h.heading}</span>
                    ) : null}
                  </div>
                  <div className="text-xs text-[var(--color-fg-muted)] line-clamp-2 mt-0.5">
                    {h.body.slice(0, 200)}
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        ) : q ? (
          <div className="px-3 py-2 text-xs text-[var(--color-fg-muted)]">No matches.</div>
        ) : (
          <div className="px-3 py-2 text-xs text-[var(--color-fg-muted)]">
            Type to search. Press <kbd>Esc</kbd> to close.
          </div>
        )}
      </div>
    </div>
  );
}
