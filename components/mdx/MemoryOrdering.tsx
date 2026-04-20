import type { ReactNode } from 'react';

export function MemoryOrdering({
  ordering,
  caption,
  children,
}: {
  ordering: string;
  caption?: string;
  children: ReactNode;
}) {
  return (
    <figure className="not-prose my-5 u-panel overflow-hidden">
      <div className="px-4 py-2 border-b border-[var(--color-border)] flex items-baseline gap-3">
        <span className="u-accent-chip">{ordering}</span>
        <span className="text-xs text-[var(--color-fg-muted)]">
          Reorderings across this operation
        </span>
      </div>
      <table className="w-full text-sm font-mono">
        <thead>
          <tr className="text-left text-xs text-[var(--color-fg-muted)]">
            <th className="px-4 py-2 font-normal">From</th>
            <th className="px-4 py-2 font-normal">Past</th>
            <th className="px-4 py-2 font-normal">To</th>
            <th className="px-4 py-2 font-normal">Allowed?</th>
            <th className="px-4 py-2 font-normal">Reason</th>
          </tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
      {caption ? (
        <figcaption className="px-4 py-2 text-xs text-[var(--color-fg-muted)] border-t border-[var(--color-border)] font-sans">
          {caption}
        </figcaption>
      ) : null}
    </figure>
  );
}

export function MOPair({
  from,
  to,
  allowed,
  note,
}: {
  from: string;
  to: string;
  allowed: 'yes' | 'no';
  note?: string;
}) {
  const yes = allowed === 'yes';
  return (
    <tr className="border-t border-[var(--color-border)]">
      <td className="px-4 py-2">{from}</td>
      <td className="px-4 py-2 text-[var(--color-fg-muted)]">→</td>
      <td className="px-4 py-2">{to}</td>
      <td
        className={`px-4 py-2 ${yes ? 'text-[var(--color-warn)]' : 'text-[var(--color-ok)]'}`}
      >
        {yes ? 'yes — assertion can fire' : 'no — forbidden'}
      </td>
      <td className="px-4 py-2 text-[var(--color-fg-muted)] font-sans">{note ?? ''}</td>
    </tr>
  );
}
