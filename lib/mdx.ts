import { compileMDX } from 'next-mdx-remote/rsc';
import remarkGfm from 'remark-gfm';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import rehypeSlug from 'rehype-slug';
import rehypeAutolinkHeadings from 'rehype-autolink-headings';
import rehypeShiki from '@shikijs/rehype';
import { mdxComponents } from '@/components/mdx';

export async function renderMDX(source: string) {
  const { content } = await compileMDX({
    source,
    components: mdxComponents,
    options: {
      parseFrontmatter: false,
      mdxOptions: {
        remarkPlugins: [remarkGfm, remarkMath],
        rehypePlugins: [
          [
            rehypeShiki,
            {
              themes: {
                dark: 'github-dark-dimmed',
                light: 'github-light',
              },
              defaultColor: false,
            },
          ],
          rehypeSlug,
          [
            rehypeAutolinkHeadings,
            {
              behavior: 'append',
              properties: { className: 'anchor', 'aria-label': 'Permalink' },
              content: { type: 'text', value: '#' },
            },
          ],
          rehypeKatex,
        ],
      },
    },
  });
  return content;
}
