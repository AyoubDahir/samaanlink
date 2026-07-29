'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useSession } from '@/lib/session';
import { Navbar } from '@/components/layout/navbar';

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const session = useSession();
  const router = useRouter();
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    setChecked(true);
  }, []);

  useEffect(() => {
    if (checked && !session) {
      router.replace('/sign-in');
    }
  }, [checked, session, router]);

  if (!checked || !session) {
    return (
      <div className='text-muted-foreground flex h-dvh items-center justify-center text-sm'>
        Loading…
      </div>
    );
  }

  return (
    <>
      <Navbar />
      <main className='mx-auto max-w-5xl px-4 py-6'>{children}</main>
    </>
  );
}
