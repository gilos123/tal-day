import Link from 'next/link';

export default function NotFound() {
  return (
    <main className="min-h-dvh grid place-items-center px-6">
      <div className="text-center font-mono">
        <h1 className="text-5xl text-[var(--color-accent)]">404</h1>
        <p className="mt-2 text-[var(--color-fg-muted)]">That lesson doesn&rsquo;t exist.</p>
        <Link href="/" className="accent-link mt-4 inline-block">
          ← back to index
        </Link>
      </div>
    </main>
  );
}
