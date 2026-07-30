'use client';

import { useEffect, useState } from 'react';

/** productId -> quantity. Client-side only until checkout, when it's synced to a real draft order. */
export type Cart = Record<string, number>;

const STORAGE_KEY = 'samaanlink_cart';
const CART_EVENT = 'samaanlink-cart-changed';

function readCart(): Cart {
  if (typeof window === 'undefined') return {};
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) return {};
  try {
    return JSON.parse(raw) as Cart;
  } catch {
    return {};
  }
}

function writeCart(cart: Cart): void {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(cart));
  window.dispatchEvent(new Event(CART_EVENT));
}

export function clearCart(): void {
  writeCart({});
}

/** Reactive cart state plus mutators; updates across tabs and after any mutation. */
export function useCart() {
  const [cart, setCart] = useState<Cart>({});

  useEffect(() => {
    setCart(readCart());
    const handler = () => setCart(readCart());
    window.addEventListener(CART_EVENT, handler);
    window.addEventListener('storage', handler);
    return () => {
      window.removeEventListener(CART_EVENT, handler);
      window.removeEventListener('storage', handler);
    };
  }, []);

  function setQuantity(productId: string, quantity: number) {
    const next = { ...readCart() };
    if (quantity <= 0) delete next[productId];
    else next[productId] = quantity;
    writeCart(next);
  }

  function increment(productId: string) {
    const current = readCart();
    setQuantity(productId, (current[productId] ?? 0) + 1);
  }

  function decrement(productId: string) {
    const current = readCart();
    setQuantity(productId, (current[productId] ?? 0) - 1);
  }

  const itemCount = Object.values(cart).reduce((sum, q) => sum + q, 0);

  return { cart, itemCount, increment, decrement, setQuantity, clear: clearCart };
}
