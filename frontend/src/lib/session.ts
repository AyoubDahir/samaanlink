'use client';

import { useEffect, useState } from 'react';

export type Role = 'customer' | 'admin';

export interface Session {
  key: string;
  role: Role;
  email: string;
  customerId?: number;
  firstName?: string;
}

const STORAGE_KEY = 'samaanlink_session';
const SESSION_EVENT = 'samaanlink-session-changed';

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
