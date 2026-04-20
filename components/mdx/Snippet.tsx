import { promises as fs } from 'node:fs';
import path from 'node:path';
import type { BundledLanguage } from 'shiki';
import { CodeBlock } from './CodeBlock';

export async function Snippet({
  file,
  lang,
  caption,
  highlightLines,
  stripHeader,
}: {
  file: string;
  lang?: BundledLanguage;
  caption?: string;
  highlightLines?: number[] | string;
  stripHeader?: boolean;
}) {
  const abs = path.join(process.cwd(), 'snippets', file);
  let code = await fs.readFile(abs, 'utf8');
  if (stripHeader) {
    code = code.replace(/^\/\*[\s\S]*?\*\/\n*/, '');
  }
  return (
    <CodeBlock
      code={code}
      lang={lang ?? 'c'}
      filename={`snippets/${file}`}
      caption={caption}
      highlightLines={highlightLines}
    />
  );
}
