import type { ReactNode } from 'react';

export function Benchmark({
  title,
  note,
  children,
}: {
  title: string;
  note?: string;
  children: ReactNode;
}) {
  return (
    <figure className="not-prose my-5 u-panel overflow-hidden">
      <div className="px-4 py-2 border-b border-[var(--color-border)] text-xs uppercase tracking-wider text-[var(--color-fg-muted)]">
        {title}
      </div>
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-xs text-[var(--color-fg-muted)]">
            <th className="px-4 py-2 font-normal">Variant</th>
            <th className="px-4 py-2 font-normal">Measured</th>
            <th className="px-4 py-2 font-normal">Note</th>
          </tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
      {note ? (
        <figcaption className="px-4 py-2 text-xs text-[var(--color-fg-muted)] border-t border-[var(--color-border)]">
          {note}
        </figcaption>
      ) : null}
    </figure>
  );
}

export function BenchRow({
  label,
  value,
  note,
}: {
  label: string;
  value: string;
  note?: string;
}) {
  return (
    <tr className="border-t border-[var(--color-border)]">
      <td className="px-4 py-2 font-mono">{label}</td>
      <td className="px-4 py-2 font-mono text-[var(--color-accent)]">{value}</td>
      <td className="px-4 py-2 text-[var(--color-fg-muted)]">{note ?? ''}</td>
    </tr>
  );
}
