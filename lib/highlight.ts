import { codeToHtml, type BundledLanguage } from 'shiki';

export const SHIKI_THEMES = { dark: 'github-dark-dimmed', light: 'github-light' } as const;

export async function highlight(code: string, lang: BundledLanguage = 'c'): Promise<string> {
  return codeToHtml(code, {
    lang,
    themes: SHIKI_THEMES,
    defaultColor: false,
  });
}
