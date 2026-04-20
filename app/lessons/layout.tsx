import { Sidebar } from '@/components/layout/Sidebar';
import { ThemeToggle } from '@/components/layout/ThemeToggle';
import { SearchModal } from '@/components/search/SearchModal';
import Link from 'next/link';

export default function LessonsLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-dvh grid md:grid-cols-[260px_1fr]">
      <aside className="hidden md:block border-r border-[var(--color-border)] px-4 bg-[var(--color-bg)]">
        <Sidebar />
      </aside>
      <div>
        <header className="sticky top-0 z-10 bg-[var(--color-bg)]/90 backdrop-blur border-b border-[var(--color-border)] px-6 py-2 flex items-center justify-between">
          <Link
            href="/"
            className="text-xs font-mono text-[var(--color-fg-muted)] hover:text-[var(--color-fg)]"
          >
            Concurrency in C
          </Link>
          <div className="flex items-center gap-2 text-xs font-mono text-[var(--color-fg-muted)]">
            <kbd>/</kbd> search <span className="mx-1">·</span> <kbd>n</kbd>/<kbd>p</kbd> nav{' '}
            <span className="mx-1">·</span> <kbd>?</kbd> help
            <ThemeToggle />
          </div>
        </header>
        <main className="px-6 md:px-10 py-8 max-w-5xl">{children}</main>
        <SearchModal />
      </div>
    </div>
  );
}
