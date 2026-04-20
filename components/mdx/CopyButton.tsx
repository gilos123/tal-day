'use client';

import { useState } from 'react';

export function CopyButton({ text, label = 'copy' }: { text: string; label?: string }) {
  const [status, setStatus] = useState<'idle' | 'ok' | 'err'>('idle');

  async function copy() {
    try {
      await navigator.clipboard.writeText(text);
      setStatus('ok');
      setTimeout(() => setStatus('idle'), 1200);
    } catch {
      setStatus('err');
      setTimeout(() => setStatus('idle'), 1500);
    }
  }

  return (
    <button
      type="button"
      onClick={copy}
      aria-label="Copy code to clipboard"
      className="text-xs font-mono px-2 py-0.5 rounded-[var(--radius-sm)] border border-[var(--color-border)] bg-[var(--color-surface-2)] text-[var(--color-fg-muted)] hover:text-[var(--color-fg)] hover:border-[var(--color-border-strong)] transition-colors"
    >
      {status === 'ok' ? 'copied' : status === 'err' ? 'error' : label}
    </button>
  );
}
