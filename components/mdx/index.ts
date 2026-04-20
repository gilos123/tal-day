import type { MDXComponents } from 'mdx/types';
import { Aside } from './Aside';
import { CodeBlock } from './CodeBlock';
import { Snippet } from './Snippet';
import { Diagram } from './Diagram';
import { Exercise, Hint, Solution } from './Exercise';
import { Benchmark, BenchRow } from './Benchmark';
import { MemoryOrdering, MOPair } from './MemoryOrdering';
import { ThreadInterleaving } from '@/components/interactive/ThreadInterleaving';
import { MemoryOrderingPlayground } from '@/components/interactive/MemoryOrderingPlayground';
import { TreiberStackViz } from '@/components/interactive/TreiberStackViz';
import { CacheCoherence } from '@/components/interactive/CacheCoherence';

export const mdxComponents: MDXComponents = {
  Aside,
  CodeBlock: CodeBlock as unknown as MDXComponents['CodeBlock'],
  Snippet: Snippet as unknown as MDXComponents['Snippet'],
  Diagram,
  Exercise,
  Hint,
  Solution,
  Benchmark,
  BenchRow,
  MemoryOrdering,
  MOPair,
  ThreadInterleaving,
  MemoryOrderingPlayground,
  TreiberStackViz,
  CacheCoherence,
};
