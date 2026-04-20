'use client';

import { useMemo, useState } from 'react';

type Op = { tid: 0 | 1; kind: 'load' | 'add' | 'store'; label: string };

/**
 * Fixed program: each thread runs tmp = x; tmp = tmp + 1; x = tmp.
 * The user picks an interleaving (a permutation of ops), and we
 * simulate to show the final value of x.
 */
const T0: Op[] = [
  { tid: 0, kind: 'load', label: 'T0: r0 = x' },
  { tid: 0, kind: 'add', label: 'T0: r0 = r0 + 1' },
  { tid: 0, kind: 'store', label: 'T0: x = r0' },
];
const T1: Op[] = [
  { tid: 1, kind: 'load', label: 'T1: r1 = x' },
  { tid: 1, kind: 'add', label: 'T1: r1 = r1 + 1' },
  { tid: 1, kind: 'store', label: 'T1: x = r1' },
];

const PRESETS: { name: string; order: Array<0 | 1> }[] = [
  { name: 'Sequential (T0, then T1) — correct', order: [0, 0, 0, 1, 1, 1] },
  { name: 'Fully interleaved — lost update', order: [0, 1, 0, 1, 0, 1] },
  { name: 'T0 reads, both write — lost update', order: [0, 1, 1, 0, 0, 1] },
];

function simulate(order: Array<0 | 1>) {
  let x = 0;
  let r0 = 0;
  let r1 = 0;
  const steps: { after: Op; state: { x: number; r0: number; r1: number } }[] = [];
  const i0 = { cursor: 0 };
  const i1 = { cursor: 0 };
  for (const tid of order) {
    const op = tid === 0 ? T0[i0.cursor++] : T1[i1.cursor++];
    if (!op) continue;
    if (op.kind === 'load') {
      if (tid === 0) r0 = x;
      else r1 = x;
    } else if (op.kind === 'add') {
      if (tid === 0) r0 = r0 + 1;
      else r1 = r1 + 1;
    } else {
      if (tid === 0) x = r0;
      else x = r1;
    }
    steps.push({ after: op, state: { x, r0, r1 } });
  }
  return steps;
}

export function ThreadInterleaving() {
  const [presetIdx, setPresetIdx] = useState(1);
  const [step, setStep] = useState(0);
  const order = PRESETS[presetIdx].order;
  const steps = useMemo(() => simulate(order), [order]);
  const state = step === 0 ? { x: 0, r0: 0, r1: 0 } : steps[step - 1].state;
  const final = steps[steps.length - 1].state.x;

  return (
    <figure className="not-prose my-6 u-panel p-4">
      <div className="flex flex-wrap items-baseline gap-3 mb-3">
        <span className="u-accent-chip">Thread interleaving</span>
        <label className="text-xs text-[var(--color-fg-muted)] flex items-center gap-2">
          preset:
          <select
            value={presetIdx}
            onChange={(e) => {
              setPresetIdx(Number(e.target.value));
              setStep(0);
            }}
            className="bg-[var(--color-surface-2)] border border-[var(--color-border)] rounded-[var(--radius-sm)] text-xs px-2 py-0.5"
          >
            {PRESETS.map((p, i) => (
              <option key={i} value={i}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="grid grid-cols-[1fr_1fr_auto] gap-4">
        <ThreadColumn
          title="Thread 0"
          ops={T0}
          completed={order
            .slice(0, step)
            .reduce<number>((acc, tid) => acc + (tid === 0 ? 1 : 0), 0)}
        />
        <ThreadColumn
          title="Thread 1"
          ops={T1}
          completed={order
            .slice(0, step)
            .reduce<number>((acc, tid) => acc + (tid === 1 ? 1 : 0), 0)}
        />
        <aside className="min-w-[12ch] text-sm font-mono u-panel p-3">
          <div className="text-xs uppercase tracking-wider text-[var(--color-fg-muted)] mb-2">
            state
          </div>
          <div>
            <span className="text-[var(--color-fg-muted)]">x = </span>
            <span className="text-[var(--color-accent)]">{state.x}</span>
          </div>
          <div>
            <span className="text-[var(--color-fg-muted)]">r0 = </span>
            {state.r0}
          </div>
          <div>
            <span className="text-[var(--color-fg-muted)]">r1 = </span>
            {state.r1}
          </div>
        </aside>
      </div>

      <div className="mt-4 font-mono text-sm">
        <div className="text-xs text-[var(--color-fg-muted)] mb-1">schedule</div>
        <ol className="flex flex-wrap gap-1">
          {order.map((tid, i) => (
            <li
              key={i}
              aria-current={i === step - 1 ? 'step' : undefined}
              className={`px-2 py-0.5 rounded-[var(--radius-sm)] border ${
                i < step
                  ? 'border-[var(--color-accent)] text-[var(--color-accent)]'
                  : 'border-[var(--color-border)] text-[var(--color-fg-muted)]'
              }`}
            >
              {steps[i]?.after.label ?? `T${tid}`}
            </li>
          ))}
        </ol>
      </div>

      <div className="mt-3 flex items-center gap-2 text-xs font-mono">
        <button
          type="button"
          onClick={() => setStep(0)}
          className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-border)]"
        >
          reset
        </button>
        <button
          type="button"
          onClick={() => setStep((s) => Math.max(0, s - 1))}
          disabled={step === 0}
          className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-border)] disabled:opacity-40"
        >
          ← step
        </button>
        <button
          type="button"
          onClick={() => setStep((s) => Math.min(steps.length, s + 1))}
          disabled={step === steps.length}
          className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-border)] disabled:opacity-40"
        >
          step →
        </button>
        <button
          type="button"
          onClick={() => setStep(steps.length)}
          className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-border)]"
        >
          run to end
        </button>
        {step === steps.length ? (
          <span className="ml-3">
            final x = <span className="text-[var(--color-accent)]">{final}</span>
            {final === 2 ? ' ✓ expected' : ' — lost update'}
          </span>
        ) : null}
      </div>

      <p className="sr-only">
        Interactive stepper for a two-thread increment of shared variable x. Step through
        the chosen interleaving using the step buttons. Final value of x is shown on
        completion.
      </p>
    </figure>
  );
}

function ThreadColumn({
  title,
  ops,
  completed,
}: {
  title: string;
  ops: Op[];
  completed: number;
}) {
  return (
    <div className="font-mono text-sm u-panel p-3">
      <div className="text-xs uppercase tracking-wider text-[var(--color-fg-muted)] mb-2">
        {title}
      </div>
      <ol className="space-y-1">
        {ops.map((op, i) => (
          <li
            key={i}
            className={
              i < completed
                ? 'text-[var(--color-accent)]'
                : i === completed
                ? 'text-[var(--color-fg)]'
                : 'text-[var(--color-fg-dim)]'
            }
          >
            {op.label}
          </li>
        ))}
      </ol>
    </div>
  );
}
