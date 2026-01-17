// src/app/[locale]/page.tsx
import { redirect } from 'next/navigation';

export default async function HomePage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;  // Await the Promise
  redirect(`/${locale}/login`);
}