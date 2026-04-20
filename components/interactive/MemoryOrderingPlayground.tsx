'use client';

import { useState } from 'react';

type Ordering = 'relaxed' | 'release/acquire' | 'seq_cst';

/**
 * Model program:
 *   // Thread 1
 *   data = 42;              // plain write A
 *   atomic_store(&flag, 1); // atomic write B
 *
 *   // Thread 2
 *   while (atomic_load(&flag) == 0);  // atomic read C
 *   assert(data == 42);               // plain read D
 *
 * For each ordering applied to B/C, we show whether the reordering
 * patterns are permitted that would let thread 2 observe data != 42.
 */

type Scenario = {
  name: string;
  reorderings: { desc: string; allowedBy: Ordering[] }[];
  outcome: Record<Ordering, string>;
};

const SCENARIO: Scenario = {
  name: 'Store-release / load-acquire publication',
  reorderings: [
    {
      desc: 'Thread 1 reorders A after B (write-write)',
      allowedBy: ['relaxed'],
    },
    {
      desc: 'Thread 2 reorders D before C (read-read speculation)',
      allowedBy: ['relaxed'],
    },
    {
      desc: 'Either thread fences an unrelated write across the atomic',
      allowedBy: ['relaxed'],
    },
  ],
  outcome: {
    relaxed: 'Assertion CAN fire — nothing synchronizes A with D. On ARM/POWER this is observed in practice.',
    'release/acquire':
      'Assertion CANNOT fire. B synchronizes-with C; A happens-before D via the release-acquire pair.',
    seq_cst:
      'Assertion CANNOT fire, and additionally there is a single total order across all seq_cst operations in the program.',
  },
};

const OPTS: Ordering[] = ['relaxed', 'release/acquire', 'seq_cst'];

export function MemoryOrderingPlayground() {
  const [ord, setOrd] = useState<Ordering>('release/acquire');

  return (
    <figure className="not-prose my-6 u-panel p-4">
      <div className="flex flex-wrap items-baseline gap-3 mb-3">
        <span className="u-accent-chip">Ordering</span>
        <span className="text-xs text-[var(--color-fg-muted)]">{SCENARIO.name}</span>
      </div>

      <div className="grid grid-cols-2 gap-3 font-mono text-sm">
        <pre className="u-panel p-3 overflow-x-auto">
{`// Thread 1
data = 42;
atomic_store_explicit(
  &flag, 1,
  memory_order_${ordKey(ord)});`}
        </pre>
        <pre className="u-panel p-3 overflow-x-auto">
{`// Thread 2
while (atomic_load_explicit(
         &flag,
         memory_order_${ordLoad(ord)}) == 0);
assert(data == 42);`}
        </pre>
      </div>

      <div className="mt-3 flex items-center gap-2 text-xs font-mono flex-wrap">
        <span className="text-[var(--color-fg-muted)]">store/load ordering:</span>
        {OPTS.map((o) => (
          <button
            key={o}
            type="button"
            onClick={() => setOrd(o)}
            aria-pressed={o === ord}
            className={`px-2 py-1 rounded-[var(--radius-sm)] border ${
              o === ord
                ? 'border-[var(--color-accent)] text-[var(--color-accent)]'
                : 'border-[var(--color-border)] text-[var(--color-fg-muted)]'
            }`}
          >
            {o}
          </button>
        ))}
      </div>

      <table className="mt-4 w-full text-sm">
        <thead>
          <tr className="text-left text-xs text-[var(--color-fg-muted)]">
            <th className="px-2 py-1 font-normal">Reordering</th>
            <th className="px-2 py-1 font-normal">Permitted under current ordering?</th>
          </tr>
        </thead>
        <tbody>
          {SCENARIO.reorderings.map((r) => {
            const allowed = r.allowedBy.includes(ord);
            return (
              <tr key={r.desc} className="border-t border-[var(--color-border)]">
                <td className="px-2 py-1 font-mono">{r.desc}</td>
                <td className="px-2 py-1">
                  {allowed ? (
                    <span className="text-[var(--color-warn)]">yes — assertion can fire</span>
                  ) : (
                    <span className="text-[var(--color-ok)]">no — forbidden</span>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <p className="mt-4 text-sm">
        <span className="text-xs uppercase tracking-wider text-[var(--color-fg-muted)] mr-2">
          verdict
        </span>
        {SCENARIO.outcome[ord]}
      </p>
    </figure>
  );
}

function ordKey(o: Ordering): string {
  if (o === 'relaxed') return 'relaxed';
  if (o === 'release/acquire') return 'release';
  return 'seq_cst';
}
function ordLoad(o: Ordering): string {
  if (o === 'relaxed') return 'relaxed';
  if (o === 'release/acquire') return 'acquire';
  return 'seq_cst';
}
