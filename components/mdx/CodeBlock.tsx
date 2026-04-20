import type { BundledLanguage } from 'shiki';
import { highlight } from '@/lib/highlight';
import { lineDiff } from '@/lib/diff';
import { CopyButton } from './CopyButton';
import { DiffToggle } from './DiffToggle';

type Props = {
  code: string;
  lang?: BundledLanguage;
  filename?: string;
  caption?: string;
  prev?: string;
  highlightLines?: number[] | string;
};

export async function CodeBlock({
  code,
  lang = 'c',
  filename,
  caption,
  prev,
  highlightLines,
}: Props) {
  const trimmed = code.replace(/\n+$/, '');
  const html = await highlight(trimmed, lang);
  const lines = Array.isArray(highlightLines)
    ? highlightLines
    : typeof highlightLines === 'string'
    ? highlightLines
        .split(',')
        .map((s) => Number(s.trim()))
        .filter((n) => Number.isFinite(n))
    : [];
  const highlighted = lines.length ? applyLineHighlights(html, lines) : html;

  const header = (filename || caption) && (
    <div className="flex items-center justify-between gap-3 mb-1 text-xs font-mono">
      <span className="text-[var(--color-fg-muted)]">
        {filename ? (
          <span className="font-semibold text-[var(--color-fg)]">{filename}</span>
        ) : null}
        {filename && caption ? ' — ' : null}
        {caption}
      </span>
      <CopyButton text={trimmed} />
    </div>
  );

  const full = (
    <div className="not-prose my-4">
      {header}
      <div
        className="relative"
        dangerouslySetInnerHTML={{ __html: highlighted }}
        aria-label={`code block${filename ? `: ${filename}` : ''}`}
      />
      {!header && (
        <div className="mt-1 flex justify-end">
          <CopyButton text={trimmed} />
        </div>
      )}
    </div>
  );

  if (!prev) return full;

  const diffLines = lineDiff(prev, trimmed);
  const diffCode = diffLines
    .map((l) => `${l.kind === 'add' ? '+ ' : l.kind === 'del' ? '- ' : '  '}${l.text}`)
    .join('\n');
  const diffHtml = await highlight(diffCode, 'diff');

  const diffView = (
    <div className="not-prose my-4">
      {header}
      <div
        className="relative"
        dangerouslySetInnerHTML={{ __html: diffHtml }}
        aria-label="diff from previous version"
      />
    </div>
  );

  return <DiffToggle full={full} diff={diffView} />;
}

function applyLineHighlights(html: string, lines: number[]): string {
  const set = new Set(lines);
  let i = 0;
  return html.replace(/<span class="line"/g, (m) => {
    i++;
    return set.has(i)
      ? `<span class="line" style="background: rgba(194,65,12,0.12); display:block;"`
      : m;
  });
}
