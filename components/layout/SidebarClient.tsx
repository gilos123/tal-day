'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { CompletionIndicator } from './CompletionIndicator';

const SECTION_BREAKS: Record<number, string> = {
  1: 'Foundations',
  5: 'Memory model',
  9: 'Locks & lock-free',
  17: 'Systems practice',
};

export type SidebarItem = {
  slug: string;
  title: string;
  order: number;
  status: 'full' | 'stub';
};

export function SidebarClient({ lessons }: { lessons: SidebarItem[] }) {
  const pathname = usePathname();
  return (
    <nav
      aria-label="Lessons"
      className="text-sm font-mono sticky top-0 max-h-dvh overflow-y-auto py-6 pr-3 pl-1"
    >
      <Link
        href="/"
        className="block text-xs uppercase tracking-[0.16em] text-[var(--color-fg-muted)] mb-5 hover:text-[var(--color-fg)]"
      >
        ← Concurrency in C
      </Link>
      <ol className="space-y-0.5">
        {lessons.map((l) => {
          const active = pathname === `/lessons/${l.slug}`;
          const sectionLabel = SECTION_BREAKS[l.order];
          return (
            <li key={l.slug}>
              {sectionLabel ? (
                <div className="mt-5 mb-1.5 text-[10px] uppercase tracking-[0.2em] text-[var(--color-accent)]">
                  {sectionLabel}
                </div>
              ) : null}
              <Link
                href={`/lessons/${l.slug}`}
                aria-current={active ? 'page' : undefined}
                className={`flex items-start gap-2 px-2 py-1.5 rounded-[var(--radius-sm)] leading-tight border-l-2 transition-colors ${
                  active
                    ? 'bg-[var(--color-surface-2)] text-[var(--color-fg)] border-[var(--color-accent)]'
                    : 'text-[var(--color-fg-muted)] hover:text-[var(--color-fg)] hover:bg-[var(--color-surface-2)] border-transparent'
                }`}
              >
                <CompletionIndicator slug={l.slug} className="mt-1.5 shrink-0" />
                <span className="min-w-0">
                  <span className="text-[var(--color-fg-dim)] mr-1.5">
                    {String(l.order).padStart(2, '0')}
                  </span>
                  <span className={l.status === 'stub' ? 'opacity-70' : ''}>
                    {l.title}
                  </span>
                </span>
              </Link>
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
