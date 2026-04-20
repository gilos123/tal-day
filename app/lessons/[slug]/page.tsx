import { notFound } from 'next/navigation';
import Link from 'next/link';
import type { Metadata } from 'next';
import { getAdjacent, getLesson, getLessonSlugs } from '@/lib/lessons';
import { renderMDX } from '@/lib/mdx';
import { MarkComplete } from '@/components/layout/MarkComplete';
import { KeyboardShortcuts } from '@/components/layout/KeyboardShortcuts';

export const dynamicParams = false;

export async function generateStaticParams() {
  const slugs = await getLessonSlugs();
  return slugs.map((slug) => ({ slug }));
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const lesson = await getLesson(slug);
  if (!lesson) return {};
  return {
    title: lesson.title,
    description: lesson.summary,
  };
}

function estimateReadingMinutes(body: string): number {
  const words = body.split(/\s+/).length;
  return Math.max(2, Math.round(words / 220));
}

export default async function LessonPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const lesson = await getLesson(slug);
  if (!lesson) notFound();

  const [content, adjacent] = await Promise.all([
    renderMDX(lesson.body),
    getAdjacent(slug),
  ]);
  const minutes = estimateReadingMinutes(lesson.body);

  return (
    <article className="max-w-[72ch]">
      <nav aria-label="breadcrumb" className="text-xs font-mono text-[var(--color-fg-muted)] mb-4">
        <Link href="/" className="hover:text-[var(--color-fg)]">Concurrency in C</Link>
        <span className="mx-2 opacity-50">/</span>
        <span>Lesson {String(lesson.order).padStart(2, '0')}</span>
      </nav>

      <header className="mb-10 pb-6 border-b border-[var(--color-border)]">
        <div className="flex items-center gap-2 text-[11px] font-mono uppercase tracking-wider text-[var(--color-fg-muted)] mb-3">
          {lesson.status === 'stub' ? (
            <span className="u-accent-chip" style={{ background: 'var(--color-surface-2)', color: 'var(--color-fg-muted)' }}>
              stub
            </span>
          ) : (
            <span className="u-accent-chip">full</span>
          )}
          <span>~{minutes} min read</span>
          {lesson.tags.length > 0 ? (
            <>
              <span aria-hidden className="opacity-40">·</span>
              <span className="flex flex-wrap gap-2">
                {lesson.tags.map((t) => (
                  <span key={t} className="text-[var(--color-fg-dim)]">#{t}</span>
                ))}
              </span>
            </>
          ) : null}
        </div>
        <h1 className="text-3xl md:text-4xl font-semibold tracking-tight m-0 leading-[1.15]">
          {lesson.title}
        </h1>
        <p className="mt-3 text-[var(--color-fg-muted)] text-lg leading-snug max-w-[68ch]">
          {lesson.summary}
        </p>
      </header>

      <div className="prose-technical">{content}</div>

      <nav
        aria-label="Lesson navigation"
        className="mt-16 pt-6 border-t border-[var(--color-border)] grid grid-cols-2 gap-4 text-sm font-mono"
      >
        <div>
          {adjacent.prev ? (
            <Link
              href={`/lessons/${adjacent.prev.slug}`}
              className="block u-panel p-3 hover:border-[var(--color-accent)] transition-colors"
            >
              <div className="text-xs text-[var(--color-fg-muted)]">← previous</div>
              <div className="mt-0.5">{adjacent.prev.title}</div>
            </Link>
          ) : (
            <div />
          )}
        </div>
        <div>
          {adjacent.next ? (
            <Link
              href={`/lessons/${adjacent.next.slug}`}
              className="block u-panel p-3 text-right hover:border-[var(--color-accent)] transition-colors"
            >
              <div className="text-xs text-[var(--color-fg-muted)]">next →</div>
              <div className="mt-0.5">{adjacent.next.title}</div>
            </Link>
          ) : (
            <div />
          )}
        </div>
      </nav>

      <div className="mt-6 flex items-center justify-between">
        <MarkComplete slug={lesson.slug} />
        <Link
          href="/"
          className="text-xs font-mono text-[var(--color-fg-muted)] hover:text-[var(--color-fg)]"
        >
          ← index
        </Link>
      </div>

      <KeyboardShortcuts
        prevHref={adjacent.prev ? `/lessons/${adjacent.prev.slug}` : null}
        nextHref={adjacent.next ? `/lessons/${adjacent.next.slug}` : null}
      />
    </article>
  );
}
