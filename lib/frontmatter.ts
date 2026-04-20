import { z } from 'zod';

export const frontmatterSchema = z.object({
  title: z.string().min(1),
  slug: z.string().regex(/^\d{2}-[a-z0-9-]+$/, {
    message: 'slug must be NN-kebab-case',
  }),
  order: z.number().int().min(1).max(99),
  summary: z.string().min(1),
  status: z.enum(['full', 'stub']).default('stub'),
  tags: z.array(z.string()).default([]),
});

export type Frontmatter = z.infer<typeof frontmatterSchema>;
