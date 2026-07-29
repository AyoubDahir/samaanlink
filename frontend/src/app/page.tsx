'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { getSession } from '@/lib/session';

export default function Page() {
  const router = useRouter();

  useEffect(() => {
    const session = getSession();
    if (!session) {
      router.replace('/sign-in');
    } else {
      router.replace(session.role === 'admin' ? '/admin/categories' : '/menu');
    }
  }, [router]);

  return (
    <div className='text-muted-foreground flex h-dvh items-center justify-center text-sm'>
      Loading…
    </div>
  );
}
