'use client';

const STORAGE_KEY = 'concurrency-c:progress:v1';
const EVENT = 'concurrency-c:progress-change';

type Progress = Record<string, { completedAt: number }>;

function safeRead(): Progress {
  if (typeof window === 'undefined') return {};
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as unknown;
    if (typeof parsed !== 'object' || parsed === null) return {};
    return parsed as Progress;
  } catch {
    return {};
  }
}

function write(p: Progress) {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(p));
  window.dispatchEvent(new CustomEvent(EVENT));
}

export function isComplete(slug: string): boolean {
  return Boolean(safeRead()[slug]);
}

export function markComplete(slug: string) {
  const p = safeRead();
  p[slug] = { completedAt: Date.now() };
  write(p);
}

/**
 * Idempotent variant of {@link markComplete}: records a `completedAt` only if
 * the lesson has never been completed before. Used by MarkComplete so repeated
 * daily re-ticks don't overwrite the original first-completion timestamp.
 */
export function ensureComplete(slug: string) {
  if (isComplete(slug)) return;
  markComplete(slug);
}

export function markIncomplete(slug: string) {
  const p = safeRead();
  delete p[slug];
  write(p);
}

export function getAllCompleted(): string[] {
  return Object.keys(safeRead());
}

export function subscribe(cb: () => void): () => void {
  if (typeof window === 'undefined') return () => {};
  const handler = () => cb();
  window.addEventListener(EVENT, handler);
  window.addEventListener('storage', handler);
  return () => {
    window.removeEventListener(EVENT, handler);
    window.removeEventListener('storage', handler);
  };
}
