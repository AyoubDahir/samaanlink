'use client';

import { useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card, CardContent } from '@/components/ui/card';
import { FormButton } from '@/components/ui/form-button';
import { formatMoney } from '@/lib/utils';
import { useSession } from '@/lib/session';
import {
  getCustomer,
  increaseItemQuantity,
  decreaseItemQuantity,
  removeItemFromCart,
  clearCart,
  placeOrder,
  FoodyApiError,
  type FoodCart
} from '@/lib/api';
import { Minus, Plus, Trash2 } from 'lucide-react';

export default function CartPage() {
  const session = useSession();
  const router = useRouter();
  const [cart, setCart] = useState<FoodCart | null>(null);
  const [loading, setLoading] = useState(true);
  const [busyItemId, setBusyItemId] = useState<number | null>(null);
  const [placing, setPlacing] = useState(false);

  const loadCart = useCallback(() => {
    if (!session?.customerId) return;
    setLoading(true);
    getCustomer(session.key, session.customerId)
      .then((customer) => setCart(customer.cart ?? null))
      .catch((err) => {
        if (err instanceof FoodyApiError) toast.error(err.message);
      })
      .finally(() => setLoading(false));
  }, [session]);

  useEffect(loadCart, [loadCart]);

  async function withBusy(itemId: number, fn: () => Promise<FoodCart>) {
    setBusyItemId(itemId);
    try {
      setCart(await fn());
    } catch (err) {
      toast.error(err instanceof FoodyApiError ? err.message : 'Action failed');
    } finally {
      setBusyItemId(null);
    }
  }

  async function handlePlaceOrder() {
    if (!session?.customerId) return;
    setPlacing(true);
    try {
      const order = await placeOrder(session.key, session.customerId);
      toast.success('Order placed!');
      router.push(`/orders/${order.orderId}`);
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Could not place order'
      );
    } finally {
      setPlacing(false);
    }
  }

  const total =
    cart?.itemList.reduce((sum, i) => sum + i.cost * i.quantity, 0) ?? 0;

  if (!session) return null;

  return (
    <div>
      <PageHeader
        title='Your cart'
        description='Review items before placing your order.'
      />

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading cart…</p>
      ) : !cart || cart.itemList.length === 0 ? (
        <p className='text-muted-foreground text-sm'>
          Your cart is empty. Add items from the menu.
        </p>
      ) : (
        <div className='space-y-4'>
          {cart.itemList.map((item) => (
            <Card key={item.itemId}>
              <CardContent className='flex items-center justify-between gap-4'>
                <div className='min-w-0'>
                  <p className='font-medium'>{item.itemName}</p>
                  <p className='text-muted-foreground text-xs'>
                    {formatMoney(item.cost)} each
                  </p>
                </div>
                <div className='flex items-center gap-2'>
                  <FormButton
                    variant='outline'
                    size='icon'
                    fullWidth={false}
                    disabled={busyItemId === item.itemId || item.quantity <= 1}
                    onClick={() =>
                      cart &&
                      withBusy(item.itemId, () =>
                        decreaseItemQuantity(
                          session.key,
                          cart.cartId,
                          item.itemId,
                          1
                        )
                      )
                    }
                  >
                    <Minus className='size-3.5' />
                  </FormButton>
                  <span className='w-6 text-center text-sm font-medium'>
                    {item.quantity}
                  </span>
                  <FormButton
                    variant='outline'
                    size='icon'
                    fullWidth={false}
                    disabled={busyItemId === item.itemId}
                    onClick={() =>
                      cart &&
                      withBusy(item.itemId, () =>
                        increaseItemQuantity(
                          session.key,
                          cart.cartId,
                          item.itemId,
                          1
                        )
                      )
                    }
                  >
                    <Plus className='size-3.5' />
                  </FormButton>
                  <FormButton
                    variant='ghost'
                    size='icon'
                    fullWidth={false}
                    disabled={busyItemId === item.itemId}
                    onClick={() =>
                      cart &&
                      withBusy(item.itemId, () =>
                        removeItemFromCart(
                          session.key,
                          cart.cartId,
                          item.itemId
                        )
                      )
                    }
                  >
                    <Trash2 className='text-destructive size-3.5' />
                  </FormButton>
                </div>
              </CardContent>
            </Card>
          ))}

          <div className='flex items-center justify-between border-t pt-4'>
            <span className='text-lg font-bold'>
              Total: {formatMoney(total)}
            </span>
            <div className='flex gap-2'>
              <FormButton
                variant='outline'
                fullWidth={false}
                onClick={() =>
                  cart && clearCart(session.key, cart.cartId).then(setCart)
                }
              >
                Clear cart
              </FormButton>
              <FormButton
                fullWidth={false}
                loading={placing}
                onClick={handlePlaceOrder}
              >
                Place order
              </FormButton>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
