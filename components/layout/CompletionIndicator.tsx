'use client';

import { useEffect, useState } from 'react';
import { getAllCompleted, subscribe } from '@/lib/progress';

export function CompletionIndicator({ slug, className }: { slug: string; className?: string }) {
  const [done, setDone] = useState(false);
  useEffect(() => {
    const update = () => setDone(getAllCompleted().includes(slug));
    update();
    return subscribe(update);
  }, [slug]);
  return (
    <span
      aria-label={done ? 'completed' : 'not completed'}
      className={className}
      style={{
        display: 'inline-block',
        width: 8,
        height: 8,
        borderRadius: 999,
        background: done ? 'var(--color-accent)' : 'transparent',
        border: done ? 'none' : '1px solid var(--color-border-strong)',
      }}
    />
  );
}
