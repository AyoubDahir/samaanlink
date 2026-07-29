'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useSession, clearSession } from '@/lib/session';
import { logout as apiLogout } from '@/lib/api';
import { FormButton } from '@/components/ui/form-button';
import { ModeToggle } from '@/components/layout/ThemeToggle/theme-toggle';
import {
  ShoppingCart,
  ClipboardList,
  UtensilsCrossed,
  Store,
  LayoutGrid
} from 'lucide-react';
import { cn } from '@/lib/utils';

const customerLinks = [
  { href: '/menu', label: 'Menu', icon: UtensilsCrossed },
  { href: '/restaurants', label: 'Restaurants', icon: Store },
  { href: '/cart', label: 'Cart', icon: ShoppingCart },
  { href: '/orders', label: 'Orders', icon: ClipboardList }
];

const adminLinks = [
  { href: '/admin/categories', label: 'Categories', icon: LayoutGrid },
  { href: '/admin/items', label: 'Items', icon: UtensilsCrossed },
  { href: '/admin/restaurants', label: 'Restaurants', icon: Store }
];

export function Navbar() {
  const session = useSession();
  const pathname = usePathname();
  const router = useRouter();

  const links = session?.role === 'admin' ? adminLinks : customerLinks;

  async function handleLogout() {
    if (session) {
      try {
        await apiLogout(session.role, session.key);
      } catch {
        // best-effort — clear local session regardless of backend result
      }
    }
    clearSession();
    router.replace('/sign-in');
  }

  return (
    <header className='bg-background sticky top-0 z-40 border-b'>
      <div className='mx-auto flex h-14 max-w-5xl items-center justify-between px-4'>
        <Link
          href={session?.role === 'admin' ? '/admin/categories' : '/menu'}
          className='text-primary text-lg font-bold'
        >
          SamaanLink
        </Link>
        {session && (
          <nav className='hidden items-center gap-1 md:flex'>
            {links.map(({ href, label, icon: Icon }) => (
              <Link
                key={href}
                href={href}
                className={cn(
                  'hover:bg-accent flex items-center gap-1.5 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                  pathname === href
                    ? 'bg-accent text-accent-foreground'
                    : 'text-muted-foreground'
                )}
              >
                <Icon className='size-4' />
                {label}
              </Link>
            ))}
          </nav>
        )}
        <div className='flex items-center gap-2'>
          <ModeToggle />
          {session && (
            <FormButton
              variant='outline'
              size='sm'
              fullWidth={false}
              onClick={handleLogout}
            >
              Log out
            </FormButton>
          )}
        </div>
      </div>
      {session && (
        <nav className='flex items-center gap-1 overflow-x-auto border-t px-2 py-1 md:hidden'>
          {links.map(({ href, label, icon: Icon }) => (
            <Link
              key={href}
              href={href}
              className={cn(
                'flex shrink-0 items-center gap-1.5 rounded-md px-3 py-1.5 text-xs font-medium',
                pathname === href
                  ? 'bg-accent text-accent-foreground'
                  : 'text-muted-foreground'
              )}
            >
              <Icon className='size-3.5' />
              {label}
            </Link>
          ))}
        </nav>
      )}
    </header>
  );
}
