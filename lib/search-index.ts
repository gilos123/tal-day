'use client';

import MiniSearch from 'minisearch';

export type SearchDoc = {
  id: string;
  slug: string;
  lessonTitle: string;
  heading: string;
  anchor: string;
  body: string;
};

let cachedPromise: Promise<MiniSearch<SearchDoc>> | null = null;

export function loadSearchIndex(): Promise<MiniSearch<SearchDoc>> {
  if (cachedPromise) return cachedPromise;
  cachedPromise = (async () => {
    const res = await fetch('/search.json', { cache: 'force-cache' });
    if (!res.ok) throw new Error(`search.json: ${res.status}`);
    const docs = (await res.json()) as SearchDoc[];
    const mini = new MiniSearch<SearchDoc>({
      fields: ['lessonTitle', 'heading', 'body'],
      storeFields: ['slug', 'lessonTitle', 'heading', 'anchor', 'body'],
      searchOptions: {
        boost: { lessonTitle: 3, heading: 2 },
        prefix: true,
        fuzzy: 0.15,
      },
    });
    mini.addAll(docs);
    return mini;
  })();
  return cachedPromise;
}
