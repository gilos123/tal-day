'use client';

/**
 * Daily-reset checkbox state.
 *
 * Separate from `lib/progress.ts` by design: progress tracks the lifetime fact
 * "this lesson has been completed at least once" (never auto-resets), while this
 * module tracks the ephemeral "has this been ticked today" UI state that the
 * MarkComplete button reflects. When the UTC calendar date changes, the entire
 * set of today's ticks is cleared automatically — either lazily on the next
 * read/write, or eagerly when a tab returns to focus after midnight.
 *
 * Shape:  { lastCheckedDate: 'yyyy-MM-dd', ids: { [id]: true } }
 */

const STORAGE_KEY = 'concurrency-c:daily-checks:v1';
const EVENT = 'concurrency-c:daily-checks-change';

type DailyChecks = {
  lastCheckedDate: string;
  ids: Record<string, true>;
};

/**
 * Current calendar date in Israel (Asia/Jerusalem) as yyyy-MM-dd, regardless of
 * the device's timezone. This way a user traveling abroad still gets a reset at
 * their home midnight, not wherever the laptop happens to be set.
 */
function todayIso(): string {
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Jerusalem',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(new Date());
  const y = parts.find((p) => p.type === 'year')?.value ?? '1970';
  const m = parts.find((p) => p.type === 'month')?.value ?? '01';
  const d = parts.find((p) => p.type === 'day')?.value ?? '01';
  return `${y}-${m}-${d}`;
}

function emptyForToday(): DailyChecks {
  return { lastCheckedDate: todayIso(), ids: {} };
}

function safeRead(): DailyChecks {
  if (typeof window === 'undefined') return emptyForToday();
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return emptyForToday();
    const parsed = JSON.parse(raw) as unknown;
    if (
      typeof parsed !== 'object' ||
      parsed === null ||
      typeof (parsed as DailyChecks).lastCheckedDate !== 'string' ||
      typeof (parsed as DailyChecks).ids !== 'object' ||
      (parsed as DailyChecks).ids === null
    ) {
      return emptyForToday();
    }
    return parsed as DailyChecks;
  } catch {
    return emptyForToday();
  }
}

function write(next: DailyChecks): void {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  window.dispatchEvent(new CustomEvent(EVENT));
}

/**
 * Returns the current store for today, rotating (clearing) it first if the stored
 * date is stale. All reads go through this so callers never see yesterday's ticks.
 */
function readAndRotate(): DailyChecks {
  if (typeof window === 'undefined') return emptyForToday();
  const stored = safeRead();
  const today = todayIso();
  if (stored.lastCheckedDate === today) return stored;
  const fresh = emptyForToday();
  write(fresh);
  return fresh;
}

export function isCheckedToday(id: string): boolean {
  if (typeof window === 'undefined') return false;
  return Boolean(readAndRotate().ids[id]);
}

export function checkToday(id: string): void {
  if (typeof window === 'undefined') return;
  const current = readAndRotate();
  if (current.ids[id]) return;
  write({ lastCheckedDate: current.lastCheckedDate, ids: { ...current.ids, [id]: true } });
}

export function uncheckToday(id: string): void {
  if (typeof window === 'undefined') return;
  const current = readAndRotate();
  if (!current.ids[id]) return;
  const nextIds = { ...current.ids };
  delete nextIds[id];
  write({ lastCheckedDate: current.lastCheckedDate, ids: nextIds });
}

/**
 * Fires the callback when any daily-check changes — including cross-tab updates
 * (`storage`), same-tab writes (`EVENT`), and when the user returns to a tab that
 * was backgrounded across midnight (`visibilitychange`, so subscribers refresh
 * their UI without a manual reload).
 */
export function subscribe(cb: () => void): () => void {
  if (typeof window === 'undefined') return () => {};
  const handler = () => cb();
  const visibility = () => {
    if (document.visibilityState === 'visible') cb();
  };
  window.addEventListener(EVENT, handler);
  window.addEventListener('storage', handler);
  document.addEventListener('visibilitychange', visibility);
  return () => {
    window.removeEventListener(EVENT, handler);
    window.removeEventListener('storage', handler);
    document.removeEventListener('visibilitychange', visibility);
  };
}
