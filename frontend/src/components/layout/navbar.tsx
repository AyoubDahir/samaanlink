'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useSession, clearSession, isRestaurantRole, homeRouteFor } from '@/lib/session';
import { logout as apiLogout } from '@/lib/api';
import { FormButton } from '@/components/ui/form-button';
import { ModeToggle } from '@/components/layout/ThemeToggle/theme-toggle';
import {
  ClipboardList,
  Store,
  LayoutGrid,
  Package,
  MapPin,
  Tag,
  Truck,
  ShoppingBag
} from 'lucide-react';
import { cn } from '@/lib/utils';

const restaurantLinks = [
  { href: '/restaurant/catalogue', label: 'Catalogue', icon: Package },
  { href: '/restaurant/orders', label: 'Orders', icon: ClipboardList },
  { href: '/restaurant/addresses', label: 'Addresses', icon: MapPin }
];

const adminLinks = [
  { href: '/admin/categories', label: 'Categories', icon: LayoutGrid },
  { href: '/admin/products', label: 'Products', icon: Tag },
  { href: '/admin/restaurants', label: 'Restaurants', icon: Store },
  { href: '/admin/orders', label: 'Orders', icon: ClipboardList },
  { href: '/admin/suppliers', label: 'Suppliers', icon: Truck },
  { href: '/admin/procurement', label: 'Procurement', icon: ShoppingBag }
];

export function Navbar() {
  const session = useSession();
  const pathname = usePathname();
  const router = useRouter();

  const links = session && isRestaurantRole(session.role) ? restaurantLinks : adminLinks;

  async function handleLogout() {
    if (session) {
      try {
        await apiLogout(session.refreshToken);
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
          href={session ? homeRouteFor(session.role) : '/sign-in'}
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
          {session && (
            <span className='text-muted-foreground hidden text-xs sm:inline'>
              {session.role.replaceAll('_', ' ')}
            </span>
          )}
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
