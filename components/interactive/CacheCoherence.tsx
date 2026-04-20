'use client';

import { useState } from 'react';

type State = 'M' | 'E' | 'S' | 'I';

type Core = { state: State; value: number | '-' };
type Memory = { value: number };

type Action =
  | { core: 0 | 1; op: 'read' }
  | { core: 0 | 1; op: 'write'; value: number };

const DESCRIPTIONS: Record<State, string> = {
  M: 'Modified — this core has the only valid copy and it differs from memory',
  E: 'Exclusive — this core has the only valid copy and it matches memory',
  S: 'Shared — multiple cores hold identical, clean copies',
  I: 'Invalid — this core has no valid copy',
};

function applyAction(
  cores: [Core, Core],
  mem: Memory,
  a: Action,
): { cores: [Core, Core]; mem: Memory; note: string } {
  const self = { ...cores[a.core] };
  const otherIdx = (a.core ^ 1) as 0 | 1;
  const other = { ...cores[otherIdx] };
  const m = { ...mem };
  let note = '';

  if (a.op === 'read') {
    if (self.state === 'M' || self.state === 'E' || self.state === 'S') {
      note = `Core ${a.core} hit in state ${self.state}; no bus traffic.`;
    } else {
      if (other.state === 'M') {
        m.value = other.value as number;
        other.state = 'S';
        self.value = other.value;
        self.state = 'S';
        note = `Core ${a.core} miss; other core in M — writeback then share. Both now S.`;
      } else if (other.state === 'E') {
        other.state = 'S';
        self.value = other.value;
        self.state = 'S';
        note = `Core ${a.core} miss; other core in E — downgrade to S on both.`;
      } else if (other.state === 'S') {
        self.value = m.value;
        self.state = 'S';
        note = `Core ${a.core} miss; other core already S — add to sharers.`;
      } else {
        self.value = m.value;
        self.state = 'E';
        note = `Core ${a.core} miss; no other sharer — load exclusive.`;
      }
    }
  } else {
    const v = a.value;
    if (self.state === 'M') {
      self.value = v;
      note = `Core ${a.core} already M; write is local.`;
    } else if (self.state === 'E') {
      self.value = v;
      self.state = 'M';
      note = `Core ${a.core} was E, upgrades to M on write.`;
    } else {
      if (other.state === 'M') {
        m.value = other.value as number;
      }
      other.state = 'I';
      other.value = '-';
      self.value = v;
      self.state = 'M';
      note = `Core ${a.core} broadcasts invalidate; other core → I. Core ${a.core} now M.`;
    }
  }

  const out: [Core, Core] = a.core === 0 ? [self, other] : [other, self];
  return { cores: out, mem: m, note };
}

const STEPS: Action[] = [
  { core: 0, op: 'read' },
  { core: 1, op: 'read' },
  { core: 0, op: 'write', value: 7 },
  { core: 1, op: 'read' },
  { core: 1, op: 'write', value: 9 },
];

function actionLabel(a: Action): string {
  return a.op === 'read' ? `Core ${a.core}: read x` : `Core ${a.core}: write x = ${a.value}`;
}

export function CacheCoherence() {
  const [history, setHistory] = useState<
    { cores: [Core, Core]; mem: Memory; note: string }[]
  >([{ cores: [{ state: 'I', value: '-' }, { state: 'I', value: '-' }], mem: { value: 0 }, note: 'Start: memory holds x = 0; no core has a cached copy.' }]);

  const step = history.length - 1;
  const { cores, mem, note } = history[step];
  const nextAction = STEPS[step];

  function advance() {
    if (!nextAction) return;
    const result = applyAction(cores, mem, nextAction);
    setHistory((h) => [...h, result]);
  }
  function reset() {
    setHistory([
      {
        cores: [
          { state: 'I', value: '-' },
          { state: 'I', value: '-' },
        ],
        mem: { value: 0 },
        note: 'Start: memory holds x = 0; no core has a cached copy.',
      },
    ]);
  }

  return (
    <figure className="not-prose my-6 u-panel p-4">
      <div className="flex flex-wrap items-baseline gap-3 mb-4">
        <span className="u-accent-chip">MESI</span>
        <span className="text-xs text-[var(--color-fg-muted)]">
          Two cores, one cache line, one word of memory.
        </span>
      </div>

      <div className="grid grid-cols-2 gap-3">
        {cores.map((c, i) => (
          <div key={i} className="u-panel p-3 font-mono text-sm">
            <div className="text-xs uppercase tracking-wider text-[var(--color-fg-muted)] mb-2">
              Core {i} cache
            </div>
            <div className="flex items-baseline gap-3">
              <div
                aria-label={`state ${c.state}`}
                className={`inline-block px-2 py-0.5 rounded-[var(--radius-sm)] border text-xs ${
                  c.state === 'M'
                    ? 'border-[var(--color-danger)] text-[var(--color-danger)]'
                    : c.state === 'E'
                    ? 'border-[var(--color-accent)] text-[var(--color-accent)]'
                    : c.state === 'S'
                    ? 'border-[var(--color-ok)] text-[var(--color-ok)]'
                    : 'border-[var(--color-border)] text-[var(--color-fg-dim)]'
                }`}
              >
                {c.state}
              </div>
              <div>
                <span className="text-[var(--color-fg-muted)]">x = </span>
                {c.value}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-3 u-panel p-3 font-mono text-sm">
        <div className="text-xs uppercase tracking-wider text-[var(--color-fg-muted)] mb-1">
          memory
        </div>
        <div>
          <span className="text-[var(--color-fg-muted)]">x = </span>
          {mem.value}
        </div>
      </div>

      <div className="mt-3 text-sm">
        <div className="text-xs text-[var(--color-fg-muted)] mb-1">last transition</div>
        <p className="font-mono text-[var(--color-fg)]">{note}</p>
        {cores.some((c) => c.state !== 'I') ? (
          <p className="mt-1 text-xs text-[var(--color-fg-muted)]">
            {cores
              .map((c, i) => (c.state !== 'I' ? `Core ${i}: ${DESCRIPTIONS[c.state]}` : null))
              .filter(Boolean)
              .join(' · ')}
          </p>
        ) : null}
      </div>

      <div className="mt-3 flex flex-wrap items-center gap-2 text-xs font-mono">
        <button
          type="button"
          onClick={reset}
          className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-border)]"
        >
          reset
        </button>
        <button
          type="button"
          onClick={advance}
          disabled={!nextAction}
          className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-accent)] text-[var(--color-accent)] disabled:opacity-40"
        >
          {nextAction ? `next: ${actionLabel(nextAction)}` : 'scenario complete'}
        </button>
        <span className="text-[var(--color-fg-muted)]">
          step {step} / {STEPS.length}
        </span>
      </div>

      <p className="sr-only">
        MESI cache coherence walkthrough. Start with both caches invalid. Use the Next
        button to apply each action in sequence: Core 0 read, Core 1 read, Core 0 write,
        Core 1 read, Core 1 write. After each action, the state of each core cache, memory,
        and a description of the transition are shown.
      </p>
    </figure>
  );
}
