import Link from 'next/link';
import { getAllLessonMeta } from '@/lib/lessons';
import { ProgressDashboard } from '@/components/layout/ProgressDashboard';
import { ThemeToggle } from '@/components/layout/ThemeToggle';
import { SearchModal } from '@/components/search/SearchModal';

const SECTIONS: { title: string; range: [number, number]; blurb: string }[] = [
  {
    title: 'I · Foundations',
    range: [1, 4],
    blurb: 'The hardware facts, the pthreads toolkit, and the patterns every correct program needs.',
  },
  {
    title: 'II · The memory model',
    range: [5, 8],
    blurb: 'What the compiler and CPU are and are not allowed to reorder. Atomics and their orderings.',
  },
  {
    title: 'III · Locks and lock-free',
    range: [9, 16],
    blurb: 'From spinlocks to Chase-Lev, with the reclamation story that lock-free code can’t skip.',
  },
  {
    title: 'IV · Systems practice',
    range: [17, 21],
    blurb: 'Futexes, a working thread pool, a real TCP server, the debuggers, and the benchmarks that don’t lie.',
  },
];

export default async function HomePage() {
  const lessons = await getAllLessonMeta();

  return (
    <main className="min-h-dvh px-6 md:px-10 py-12 max-w-6xl mx-auto">
      <header className="mb-12">
        <div className="flex items-baseline justify-between gap-4 flex-wrap">
          <div>
            <h1 className="text-3xl md:text-4xl font-semibold tracking-tight m-0 leading-tight">
              Concurrency in <span className="text-[var(--color-accent)] font-mono">C</span>
            </h1>
            <p className="mt-2 text-[var(--color-fg-muted)] font-mono text-sm">
              From the machine model to lock-free data structures.
            </p>
          </div>
          <div className="flex items-center gap-2 text-xs font-mono text-[var(--color-fg-muted)]">
            <kbd>/</kbd><span>search</span>
            <span className="mx-2 opacity-40">·</span>
            <kbd>?</kbd><span>shortcuts</span>
            <ThemeToggle />
          </div>
        </div>
        <div className="mt-6 h-px bg-gradient-to-r from-[var(--color-accent)] via-[var(--color-border)] to-transparent" />
      </header>

      <div className="grid md:grid-cols-[1fr_280px] gap-10 mb-14">
        <section className="prose-technical max-w-[70ch]">
          <p>
            A code-first, reference-grade walk through concurrency in C — for
            readers who already know C and want to understand <em>why</em> an
            atomic load with <code>memory_order_acquire</code> behaves as it does,
            not just that it does.
          </p>
          <p>
            Every concept lands first as executable C. Every snippet in{' '}
            <code>snippets/</code> compiles with{' '}
            <code>gcc&nbsp;-std=c11&nbsp;-Wall&nbsp;-Wextra&nbsp;-pthread&nbsp;-fsanitize=thread</code>.
            Nothing here is hand-waved — if a claim is hard, the lesson says so,
            and a <em>broken</em> snippet usually shows why.
          </p>
          <p>
            Progress is stored locally. No account, no analytics, no cookies.
          </p>
        </section>
        <ProgressDashboard
          lessons={lessons.map((l) => ({
            slug: l.slug,
            title: l.title,
            order: l.order,
            status: l.status,
          }))}
        />
      </div>

      <div className="space-y-10">
        {SECTIONS.map((section) => {
          const inSection = lessons.filter(
            (l) => l.order >= section.range[0] && l.order <= section.range[1],
          );
          if (inSection.length === 0) return null;
          return (
            <section key={section.title}>
              <header className="mb-4 flex items-baseline justify-between gap-4 flex-wrap">
                <h2 className="text-xs uppercase tracking-[0.2em] text-[var(--color-accent)] font-mono m-0">
                  {section.title}
                </h2>
                <p className="text-sm text-[var(--color-fg-muted)] max-w-[60ch]">
                  {section.blurb}
                </p>
              </header>
              <ul className="grid md:grid-cols-2 gap-3">
                {inSection.map((l) => (
                  <li key={l.slug}>
                    <Link
                      href={`/lessons/${l.slug}`}
                      className="block u-panel p-4 hover:border-[var(--color-accent)] transition-colors group"
                    >
                      <div className="flex items-baseline gap-3 mb-1">
                        <span className="font-mono text-xs text-[var(--color-fg-dim)]">
                          {String(l.order).padStart(2, '0')}
                        </span>
                        <h3 className="text-base font-semibold m-0 group-hover:text-[var(--color-accent)] transition-colors">
                          {l.title}
                        </h3>
                        {l.status === 'stub' ? (
                          <span className="ml-auto text-[10px] font-mono uppercase tracking-wider text-[var(--color-fg-dim)]">
                            stub
                          </span>
                        ) : (
                          <span className="ml-auto text-[10px] font-mono uppercase tracking-wider text-[var(--color-accent)]">
                            full
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-[var(--color-fg-muted)] leading-snug line-clamp-2">
                        {l.summary}
                      </p>
                      {l.tags.length > 0 ? (
                        <div className="mt-2 flex flex-wrap gap-1.5">
                          {l.tags.map((t) => (
                            <span
                              key={t}
                              className="text-[10px] font-mono text-[var(--color-fg-dim)]"
                            >
                              #{t}
                            </span>
                          ))}
                        </div>
                      ) : null}
                    </Link>
                  </li>
                ))}
              </ul>
            </section>
          );
        })}
      </div>

      <footer className="mt-20 pt-6 border-t border-[var(--color-border)] text-xs text-[var(--color-fg-muted)] font-mono flex flex-wrap items-center justify-between gap-2">
        <span>
          No analytics · no cookies · progress in <code>localStorage</code>
        </span>
        <Link
          href="/lessons/01-machine-model"
          className="text-[var(--color-accent)] hover:underline"
        >
          start →
        </Link>
      </footer>
      <SearchModal />
    </main>
  );
}
