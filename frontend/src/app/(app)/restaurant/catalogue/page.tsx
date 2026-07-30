'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card } from '@/components/ui/card';
import { FormButton } from '@/components/ui/form-button';
import { useSession } from '@/lib/session';
import { useCart } from '@/lib/cart';
import {
  listCategories,
  listProducts,
  myRestaurant,
  listBranches,
  listDeliveryAddresses,
  createOrder,
  addOrderLine,
  placeOrder,
  ApiError,
  type CategorySummary,
  type ProductSummary
} from '@/lib/api';
import { cn } from '@/lib/utils';
import { Minus, Plus, ShoppingCart, Package } from 'lucide-react';

export default function RestaurantCataloguePage() {
  const session = useSession();
  const router = useRouter();
  const { cart, itemCount, increment, decrement, clear } = useCart();

  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [activeCategoryId, setActiveCategoryId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [checkingOut, setCheckingOut] = useState(false);

  useEffect(() => {
    if (!session) return;
    setLoading(true);
    Promise.all([listCategories(session.accessToken), listProducts(session.accessToken)])
      .then(([cats, prods]) => {
        setCategories(cats);
        setProducts(prods.filter((p) => p.status === 'ACTIVE'));
      })
      .catch(() => {
        setCategories([]);
        setProducts([]);
      })
      .finally(() => setLoading(false));
  }, [session]);

  const visibleProducts = activeCategoryId
    ? products.filter((p) => p.categoryId === activeCategoryId)
    : products;

  async function handleCheckout() {
    if (!session) return;
    const lines = Object.entries(cart);
    if (lines.length === 0) return;

    setCheckingOut(true);
    try {
      const restaurant = await myRestaurant(session.accessToken);
      const branches = await listBranches(session.accessToken, restaurant.id);
      const primaryBranch = branches.find((b) => b.primary) ?? branches[0];
      if (!primaryBranch) throw new ApiError('No branch found for your restaurant');

      const addresses = await listDeliveryAddresses(session.accessToken, primaryBranch.id);
      const address = addresses.find((a) => a.defaultAddress) ?? addresses[0];
      if (!address) {
        toast.error('Add a delivery address first');
        router.push('/restaurant/addresses');
        return;
      }

      const order = await createOrder(session.accessToken, restaurant.id, address.id);
      for (const [productId, quantity] of lines) {
        await addOrderLine(session.accessToken, order.id, productId, quantity);
      }
      await placeOrder(session.accessToken, order.id);

      clear();
      toast.success('Order placed');
      router.push(`/restaurant/orders/${order.id}`);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not place order');
    } finally {
      setCheckingOut(false);
    }
  }

  return (
    <div className={cn(itemCount > 0 && 'pb-24')}>
      <PageHeader title='Catalogue' description='Tap a product to add it to your order.' />

      <div className='mb-5 flex flex-wrap gap-2'>
        <button
          onClick={() => setActiveCategoryId(null)}
          className={cn(
            'rounded-full border px-3 py-1.5 text-sm font-medium transition-colors',
            activeCategoryId === null
              ? 'bg-primary text-primary-foreground border-primary'
              : 'text-muted-foreground hover:bg-accent'
          )}
        >
          All
        </button>
        {categories.map((c) => (
          <button
            key={c.id}
            onClick={() => setActiveCategoryId(c.id)}
            className={cn(
              'rounded-full border px-3 py-1.5 text-sm font-medium transition-colors',
              activeCategoryId === c.id
                ? 'bg-primary text-primary-foreground border-primary'
                : 'text-muted-foreground hover:bg-accent'
            )}
          >
            {c.name}
          </button>
        ))}
      </div>

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : visibleProducts.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No products available yet.</p>
      ) : (
        <div className='grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4'>
          {visibleProducts.map((p) => (
            <ProductCard
              key={p.id}
              product={p}
              quantity={cart[p.id] ?? 0}
              onIncrement={() => increment(p.id)}
              onDecrement={() => decrement(p.id)}
            />
          ))}
        </div>
      )}

      {itemCount > 0 && (
        <div className='fixed inset-x-0 bottom-0 z-40 border-t bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80'>
          <div className='mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-3'>
            <div className='flex items-center gap-2 text-sm font-medium'>
              <ShoppingCart className='size-5' />
              {itemCount} {itemCount === 1 ? 'item' : 'items'} in cart
            </div>
            <FormButton fullWidth={false} loading={checkingOut} onClick={handleCheckout}>
              Place order
            </FormButton>
          </div>
        </div>
      )}
    </div>
  );
}

function ProductCard({
  product,
  quantity,
  onIncrement,
  onDecrement
}: {
  product: ProductSummary;
  quantity: number;
  onIncrement: () => void;
  onDecrement: () => void;
}) {
  return (
    <Card
      className={cn(
        'gap-3 overflow-hidden py-0 transition-shadow hover:shadow-md',
        quantity > 0 && 'ring-primary ring-2'
      )}
    >
      <div className='bg-muted flex aspect-square items-center justify-center'>
        <Package className='text-muted-foreground/40 size-10' />
      </div>
      <div className='flex flex-1 flex-col gap-2 px-3 pb-3'>
        <div className='min-h-10'>
          <p className='line-clamp-2 text-sm leading-tight font-medium'>{product.name}</p>
          <p className='text-muted-foreground mt-0.5 text-xs'>
            {product.sku} · {product.sellingUnitCode}
          </p>
        </div>

        {quantity === 0 ? (
          <button
            onClick={onIncrement}
            className='bg-primary text-primary-foreground hover:bg-primary/90 flex items-center justify-center gap-1 rounded-full py-1.5 text-sm font-semibold transition-colors'
          >
            <Plus className='size-4' />
            Add
          </button>
        ) : (
          <div className='bg-primary text-primary-foreground flex items-center justify-between rounded-full px-1 py-1'>
            <button
              onClick={onDecrement}
              className='flex size-7 items-center justify-center rounded-full transition-transform active:scale-90'
              aria-label='Decrease quantity'
            >
              <Minus className='size-4' />
            </button>
            <span className='text-sm font-semibold tabular-nums'>{quantity}</span>
            <button
              onClick={onIncrement}
              className='flex size-7 items-center justify-center rounded-full transition-transform active:scale-90'
              aria-label='Increase quantity'
            >
              <Plus className='size-4' />
            </button>
          </div>
        )}
      </div>
    </Card>
  );
}
