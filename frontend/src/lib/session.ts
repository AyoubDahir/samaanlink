'use client';

import { useEffect, useState } from 'react';
import type { AppRole } from '@/lib/api';

export interface Session {
  accessToken: string;
  refreshToken: string;
  userId: string;
  role: AppRole;
}

const STORAGE_KEY = 'samaanlink_session';
const SESSION_EVENT = 'samaanlink-session-changed';

const RESTAURANT_ROLES: AppRole[] = ['RESTAURANT_OWNER', 'RESTAURANT_STAFF'];

export function isRestaurantRole(role: AppRole): boolean {
  return RESTAURANT_ROLES.includes(role);
}

/** Landing route for a freshly-authenticated session, based on role. */
export function homeRouteFor(role: AppRole): string {
  return isRestaurantRole(role) ? '/restaurant/orders' : '/admin/categories';
}

export function getSession(): Session | null {
  if (typeof window === 'undefined') return null;
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as Session;
  } catch {
    return null;
  }
}

export function setSession(session: Session): void {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  window.dispatchEvent(new Event(SESSION_EVENT));
}

export function clearSession(): void {
  if (typeof window === 'undefined') return;
  window.localStorage.removeItem(STORAGE_KEY);
  window.dispatchEvent(new Event(SESSION_EVENT));
}

/** Reactive read of the current session; updates across tabs and after setSession/clearSession. */
export function useSession(): Session | null {
  const [session, setSessionState] = useState<Session | null>(null);

  useEffect(() => {
    setSessionState(getSession());
    const handler = () => setSessionState(getSession());
    window.addEventListener(SESSION_EVENT, handler);
    window.addEventListener('storage', handler);
    return () => {
      window.removeEventListener(SESSION_EVENT, handler);
      window.removeEventListener('storage', handler);
    };
  }, []);

  return session;
}
