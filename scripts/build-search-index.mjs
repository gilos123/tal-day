#!/usr/bin/env node
/*
 * Builds public/search.json from content/lessons/*.mdx.
 * Each lesson is split on ## / ### headings into chunks; each chunk
 * becomes one MiniSearch doc. Anchor matches rehype-slug's algorithm
 * (lowercase, hyphenate whitespace, strip punctuation).
 */
import { promises as fs } from 'node:fs';
import path from 'node:path';
import matter from 'gray-matter';

const root = process.cwd();
const lessonsDir = path.join(root, 'content', 'lessons');
const outPath = path.join(root, 'public', 'search.json');

function slugify(heading) {
  return heading
    .toLowerCase()
    .replace(/`[^`]*`/g, ' ')
    .replace(/[^\w\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-');
}

function stripMdxNoise(text) {
  return text
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/<[A-Z][^>]*\/>/g, ' ')
    .replace(/<[A-Z][^>]*>/g, ' ')
    .replace(/<\/[A-Z][^>]*>/g, ' ')
    .replace(/`[^`]+`/g, (m) => m.slice(1, -1))
    .replace(/\*\*|__|\*|_/g, '')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .replace(/\s+/g, ' ')
    .trim();
}

function chunk(body, lessonTitle) {
  const lines = body.split('\n');
  const chunks = [];
  let current = { heading: lessonTitle, anchor: '', buffer: [] };
  for (const line of lines) {
    const m = line.match(/^(#{2,3})\s+(.+?)\s*$/);
    if (m) {
      if (current.buffer.length) chunks.push(current);
      const heading = m[2].replace(/`/g, '');
      current = { heading, anchor: slugify(heading), buffer: [] };
    } else {
      current.buffer.push(line);
    }
  }
  if (current.buffer.length) chunks.push(current);
  return chunks.map((c) => ({
    heading: c.heading,
    anchor: c.anchor,
    body: stripMdxNoise(c.buffer.join('\n')),
  }));
}

async function main() {
  const entries = await fs.readdir(lessonsDir);
  const mdx = entries.filter((f) => f.endsWith('.mdx'));
  const docs = [];
  for (const filename of mdx) {
    const raw = await fs.readFile(path.join(lessonsDir, filename), 'utf8');
    const { data, content } = matter(raw);
    if (!data.slug || !data.title) {
      throw new Error(`Missing frontmatter in ${filename}`);
    }
    const chunks = chunk(content, data.title);
    chunks.forEach((c, i) => {
      if (!c.body) return;
      docs.push({
        id: `${data.slug}#${c.anchor || i}`,
        slug: data.slug,
        lessonTitle: data.title,
        heading: c.heading,
        anchor: c.anchor,
        body: c.body.slice(0, 2000),
      });
    });
  }
  await fs.mkdir(path.dirname(outPath), { recursive: true });
  await fs.writeFile(outPath, JSON.stringify(docs));
  console.log(`wrote ${docs.length} search docs → ${path.relative(root, outPath)}`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
