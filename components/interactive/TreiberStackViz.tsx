'use client';

import { useState } from 'react';

type Node = { id: number; value: number };
type OpKind = 'push' | 'pop';
type Step = {
  kind: OpKind;
  phase: 'start' | 'snapshot' | 'prepare' | 'cas-ok' | 'cas-fail' | 'done';
  stack: Node[];
  local?: { oldHead?: Node | null; newHead?: Node | null; returned?: number | null };
  note: string;
};

const INITIAL: Node[] = [
  { id: 3, value: 30 },
  { id: 2, value: 20 },
  { id: 1, value: 10 },
];

function push(stack: Node[], value: number): Step[] {
  const newNode: Node = { id: stack.length ? stack[0].id + 1 : 1, value };
  const oldHead = stack[0] ?? null;
  return [
    {
      kind: 'push',
      phase: 'start',
      stack,
      note: `Allocate new node { value = ${value} }. We haven't touched the shared stack yet.`,
    },
    {
      kind: 'push',
      phase: 'snapshot',
      stack,
      local: { oldHead },
      note: 'Atomic load the current head; remember it as oldHead.',
    },
    {
      kind: 'push',
      phase: 'prepare',
      stack,
      local: { oldHead, newHead: newNode },
      note: 'Set newNode.next = oldHead (local write, not yet published).',
    },
    {
      kind: 'push',
      phase: 'cas-ok',
      stack: [newNode, ...stack],
      local: { oldHead, newHead: newNode },
      note: 'CAS(head, oldHead, newNode) succeeded — the new node is now published.',
    },
    {
      kind: 'push',
      phase: 'done',
      stack: [newNode, ...stack],
      note: 'Return. Total ordering of this push is established by the successful CAS.',
    },
  ];
}

function pop(stack: Node[]): Step[] {
  if (stack.length === 0) {
    return [
      {
        kind: 'pop',
        phase: 'start',
        stack,
        note: 'Stack is empty; pop returns EMPTY without touching head.',
      },
    ];
  }
  const oldHead = stack[0];
  const newHead = stack[1] ?? null;
  return [
    {
      kind: 'pop',
      phase: 'snapshot',
      stack,
      local: { oldHead },
      note: 'Atomic load head → oldHead.',
    },
    {
      kind: 'pop',
      phase: 'prepare',
      stack,
      local: { oldHead, newHead },
      note: 'Read oldHead.next → newHead (local). This is the value we will install.',
    },
    {
      kind: 'pop',
      phase: 'cas-ok',
      stack: stack.slice(1),
      local: { oldHead, newHead, returned: oldHead.value },
      note: 'CAS(head, oldHead, newHead) succeeded. Caller now owns the popped node — when can it be freed?',
    },
    {
      kind: 'pop',
      phase: 'done',
      stack: stack.slice(1),
      local: { returned: oldHead.value },
      note: 'Return value. (Reclamation is lesson 15.)',
    },
  ];
}

export function TreiberStackViz() {
  const [stack, setStack] = useState<Node[]>(INITIAL);
  const [steps, setSteps] = useState<Step[] | null>(null);
  const [idx, setIdx] = useState(0);
  const [pushVal, setPushVal] = useState(40);

  const current: Step | null = steps ? steps[idx] : null;
  const visibleStack = current ? current.stack : stack;
  const local = current?.local;

  function startPush() {
    const s = push(stack, pushVal);
    setSteps(s);
    setIdx(0);
  }
  function startPop() {
    const s = pop(stack);
    setSteps(s);
    setIdx(0);
  }
  function stepForward() {
    if (!steps) return;
    if (idx < steps.length - 1) setIdx((i) => i + 1);
  }
  function commit() {
    if (!steps) return;
    setStack(steps[steps.length - 1].stack);
    setSteps(null);
    setIdx(0);
    setPushVal((v) => v + 10);
  }
  function cancel() {
    setSteps(null);
    setIdx(0);
  }

  return (
    <figure className="not-prose my-6 u-panel p-4">
      <div className="flex flex-wrap items-baseline gap-3 mb-3">
        <span className="u-accent-chip">Treiber stack</span>
        <span className="text-xs text-[var(--color-fg-muted)]">
          {current ? `${current.kind} — ${current.phase}` : 'idle — pick an operation'}
        </span>
      </div>

      <div className="grid grid-cols-[auto_1fr] gap-4 items-start">
        <StackSvg stack={visibleStack} highlightTopId={steps ? steps[idx].local?.oldHead?.id ?? null : null} />

        <aside className="font-mono text-sm u-panel p-3 min-h-[10rem]">
          <div className="text-xs uppercase tracking-wider text-[var(--color-fg-muted)] mb-2">
            thread-local view
          </div>
          {local?.oldHead !== undefined ? (
            <div>
              <span className="text-[var(--color-fg-muted)]">oldHead = </span>
              {local.oldHead ? `node#${local.oldHead.id} (${local.oldHead.value})` : 'NULL'}
            </div>
          ) : null}
          {local?.newHead !== undefined ? (
            <div>
              <span className="text-[var(--color-fg-muted)]">newHead = </span>
              {local.newHead ? `node#${local.newHead.id} (${local.newHead.value})` : 'NULL'}
            </div>
          ) : null}
          {local?.returned != null ? (
            <div className="mt-2">
              <span className="text-[var(--color-fg-muted)]">returned: </span>
              <span className="text-[var(--color-accent)]">{local.returned}</span>
            </div>
          ) : null}
          {current ? <p className="mt-3 text-[var(--color-fg-muted)]">{current.note}</p> : null}
        </aside>
      </div>

      <div className="mt-4 flex flex-wrap items-center gap-2 text-xs font-mono">
        {!steps ? (
          <>
            <label className="flex items-center gap-2">
              value:
              <input
                type="number"
                value={pushVal}
                onChange={(e) => setPushVal(Number(e.target.value))}
                className="bg-[var(--color-surface-2)] border border-[var(--color-border)] rounded-[var(--radius-sm)] px-2 py-0.5 w-20"
              />
            </label>
            <button
              type="button"
              onClick={startPush}
              className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-accent)] text-[var(--color-accent)]"
            >
              push
            </button>
            <button
              type="button"
              onClick={startPop}
              className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-accent)] text-[var(--color-accent)]"
            >
              pop
            </button>
            <button
              type="button"
              onClick={() => setStack(INITIAL)}
              className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-border)]"
            >
              reset
            </button>
          </>
        ) : (
          <>
            <button
              type="button"
              onClick={stepForward}
              disabled={idx === steps.length - 1}
              className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-accent)] text-[var(--color-accent)] disabled:opacity-40"
            >
              step →
            </button>
            <button
              type="button"
              onClick={commit}
              disabled={idx < steps.length - 1}
              className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-border)] disabled:opacity-40"
            >
              commit operation
            </button>
            <button
              type="button"
              onClick={cancel}
              className="px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-border)]"
            >
              cancel
            </button>
            <span className="text-[var(--color-fg-muted)]">
              step {idx + 1} / {steps.length}
            </span>
          </>
        )}
      </div>

      <p className="sr-only">
        Treiber stack visualizer. Choose push or pop to step through the algorithm phases:
        snapshot the head, prepare the new pointer, perform the compare-and-swap, and commit.
        The current stack contents and thread-local variables are displayed textually at each step.
      </p>
    </figure>
  );
}

function StackSvg({
  stack,
  highlightTopId,
}: {
  stack: Node[];
  highlightTopId: number | null;
}) {
  const nodeW = 120;
  const nodeH = 42;
  const gap = 18;
  const topLabelH = 26;
  const height = topLabelH + stack.length * (nodeH + gap) + 30;
  const width = nodeW + 80;

  return (
    <svg
      width={width}
      height={Math.max(height, 180)}
      viewBox={`0 0 ${width} ${Math.max(height, 180)}`}
      role="img"
      aria-label={`Stack visualization: ${stack.length === 0 ? 'empty' : stack.map((n) => n.value).join(' → ')}`}
    >
      <text x="10" y="18" fill="var(--color-fg-muted)" fontSize="11" fontFamily="var(--font-mono)">
        head
      </text>
      <line
        x1="40"
        y1="22"
        x2="40"
        y2={topLabelH + 4}
        stroke="var(--color-accent)"
        strokeWidth="1.5"
      />
      {stack.length === 0 ? (
        <text
          x="60"
          y={topLabelH + 20}
          fill="var(--color-fg-muted)"
          fontSize="12"
          fontFamily="var(--font-mono)"
        >
          NULL
        </text>
      ) : null}
      {stack.map((n, i) => {
        const y = topLabelH + i * (nodeH + gap);
        const isTop = i === 0;
        const isHot = highlightTopId === n.id;
        return (
          <g key={n.id}>
            <rect
              x={60}
              y={y}
              width={nodeW}
              height={nodeH}
              rx={4}
              fill={isHot ? 'var(--color-accent-muted)' : 'var(--color-surface-2)'}
              stroke={isTop ? 'var(--color-accent)' : 'var(--color-border-strong)'}
              strokeWidth={isTop ? 2 : 1}
            />
            <text
              x={68}
              y={y + 16}
              fill="var(--color-fg-muted)"
              fontSize="10"
              fontFamily="var(--font-mono)"
            >
              node#{n.id}
            </text>
            <text
              x={68}
              y={y + 32}
              fill="var(--color-fg)"
              fontSize="14"
              fontFamily="var(--font-mono)"
            >
              value = {n.value}
            </text>
            {i < stack.length - 1 ? (
              <>
                <line
                  x1={60 + nodeW / 2}
                  y1={y + nodeH}
                  x2={60 + nodeW / 2}
                  y2={y + nodeH + gap}
                  stroke="var(--color-border-strong)"
                  strokeWidth="1"
                />
                <polygon
                  points={`${60 + nodeW / 2 - 4},${y + nodeH + gap - 4} ${60 + nodeW / 2 + 4},${y + nodeH + gap - 4} ${60 + nodeW / 2},${y + nodeH + gap}`}
                  fill="var(--color-border-strong)"
                />
              </>
            ) : (
              <text
                x={60 + nodeW / 2 + 6}
                y={y + nodeH + 14}
                fill="var(--color-fg-muted)"
                fontSize="10"
                fontFamily="var(--font-mono)"
              >
                next = NULL
              </text>
            )}
          </g>
        );
      })}
    </svg>
  );
}
