'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export function KeyboardShortcuts({
  prevHref,
  nextHref,
  onOpenSearch,
}: {
  prevHref?: string | null;
  nextHref?: string | null;
  onOpenSearch?: () => void;
}) {
  const router = useRouter();

  useEffect(() => {
    function handler(e: KeyboardEvent) {
      if (e.metaKey || e.ctrlKey || e.altKey) return;
      const t = e.target as HTMLElement | null;
      if (
        t &&
        (t.tagName === 'INPUT' ||
          t.tagName === 'TEXTAREA' ||
          t.tagName === 'SELECT' ||
          t.isContentEditable)
      ) {
        return;
      }
      switch (e.key) {
        case 'j':
          window.scrollBy({ top: 120, behavior: 'smooth' });
          break;
        case 'k':
          window.scrollBy({ top: -120, behavior: 'smooth' });
          break;
        case 'n':
          if (nextHref) {
            e.preventDefault();
            router.push(nextHref);
          }
          break;
        case 'p':
          if (prevHref) {
            e.preventDefault();
            router.push(prevHref);
          }
          break;
        case '/':
          if (onOpenSearch) {
            e.preventDefault();
            onOpenSearch();
          }
          break;
        case '?':
          alert(
            'Keyboard shortcuts:\n\nj / k  — scroll down / up\nn / p — next / previous lesson\n/     — open search\n?     — this help',
          );
          break;
      }
    }
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [prevHref, nextHref, onOpenSearch, router]);

  return null;
}
