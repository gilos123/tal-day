import { promises as fs } from 'node:fs';
import path from 'node:path';
import matter from 'gray-matter';
import { frontmatterSchema, type Frontmatter } from './frontmatter';

export const LESSONS_DIR = path.join(process.cwd(), 'content', 'lessons');

export type LessonMeta = Frontmatter & {
  filename: string;
};

export type LessonFile = LessonMeta & {
  body: string;
};

let cache: LessonMeta[] | null = null;

async function readAll(): Promise<LessonFile[]> {
  const entries = await fs.readdir(LESSONS_DIR);
  const mdxFiles = entries.filter((f) => f.endsWith('.mdx'));
  const lessons = await Promise.all(
    mdxFiles.map(async (filename) => {
      const raw = await fs.readFile(path.join(LESSONS_DIR, filename), 'utf8');
      const { data, content } = matter(raw);
      const parsed = frontmatterSchema.safeParse(data);
      if (!parsed.success) {
        throw new Error(
          `Invalid frontmatter in ${filename}: ${parsed.error.issues
            .map((i) => `${i.path.join('.')}: ${i.message}`)
            .join('; ')}`,
        );
      }
      return { ...parsed.data, filename, body: content };
    }),
  );
  return lessons.sort((a, b) => a.order - b.order);
}

export async function getAllLessonMeta(): Promise<LessonMeta[]> {
  if (cache) return cache;
  const files = await readAll();
  cache = files.map(({ body: _body, ...meta }) => meta);
  return cache;
}

export async function getLesson(slug: string): Promise<LessonFile | null> {
  const files = await readAll();
  return files.find((l) => l.slug === slug) ?? null;
}

export async function getLessonSlugs(): Promise<string[]> {
  const metas = await getAllLessonMeta();
  return metas.map((m) => m.slug);
}

export async function getAdjacent(slug: string): Promise<{
  prev: LessonMeta | null;
  next: LessonMeta | null;
}> {
  const metas = await getAllLessonMeta();
  const idx = metas.findIndex((m) => m.slug === slug);
  if (idx === -1) return { prev: null, next: null };
  return {
    prev: idx > 0 ? metas[idx - 1] : null,
    next: idx < metas.length - 1 ? metas[idx + 1] : null,
  };
}
