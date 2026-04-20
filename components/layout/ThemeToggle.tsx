'use client';

import { useEffect, useState } from 'react';

const KEY = 'concurrency-c:theme';

export function ThemeInit() {
  return (
    <script
      dangerouslySetInnerHTML={{
        __html: `(function(){try{var t=localStorage.getItem(${JSON.stringify(KEY)});if(t==='light'||t==='dark'){document.documentElement.dataset.theme=t;}}catch(e){}})();`,
      }}
    />
  );
}

export function ThemeToggle() {
  const [theme, setTheme] = useState<'dark' | 'light'>('dark');

  useEffect(() => {
    const current = document.documentElement.dataset.theme === 'light' ? 'light' : 'dark';
    setTheme(current);
  }, []);

  function toggle() {
    const next = theme === 'dark' ? 'light' : 'dark';
    setTheme(next);
    document.documentElement.dataset.theme = next;
    try {
      localStorage.setItem(KEY, next);
    } catch {}
  }

  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={`Switch to ${theme === 'dark' ? 'light' : 'dark'} theme`}
      className="text-xs font-mono px-2 py-1 rounded-[var(--radius-sm)] border border-[var(--color-border)] hover:border-[var(--color-border-strong)] text-[var(--color-fg-muted)] hover:text-[var(--color-fg)]"
    >
      {theme === 'dark' ? 'light' : 'dark'}
    </button>
  );
}
