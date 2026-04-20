import { getAllLessonMeta } from '@/lib/lessons';
import { SidebarClient } from './SidebarClient';

export async function Sidebar() {
  const lessons = await getAllLessonMeta();
  return (
    <SidebarClient
      lessons={lessons.map((l) => ({
        slug: l.slug,
        title: l.title,
        order: l.order,
        status: l.status,
      }))}
    />
  );
}
