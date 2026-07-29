'use client';

import { useEffect, useState } from 'react';
import { toast } from 'sonner';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card, CardContent, CardFooter } from '@/components/ui/card';
import { FormButton } from '@/components/ui/form-button';
import { cn, formatMoney } from '@/lib/utils';
import { useSession } from '@/lib/session';
import {
  addItemToCart,
  getAllCategories,
  getAllItems,
  getItemsByCategoryName,
  FoodyApiError,
  type Category,
  type Item
} from '@/lib/api';

export default function MenuPage() {
  const session = useSession();
  const [categories, setCategories] = useState<Category[]>([]);
  const [items, setItems] = useState<Item[]>([]);
  const [activeCategory, setActiveCategory] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [addingId, setAddingId] = useState<number | null>(null);

  useEffect(() => {
    if (!session) return;
    getAllCategories(session.key)
      .then(setCategories)
      .catch(() => setCategories([]));
  }, [session]);

  useEffect(() => {
    if (!session) return;
    setLoading(true);
    const request = activeCategory
      ? getItemsByCategoryName(session.key, activeCategory)
      : getAllItems(session.key);

    request
      .then(setItems)
      .catch((err) => {
        setItems([]);
        if (err instanceof FoodyApiError) toast.error(err.message);
      })
      .finally(() => setLoading(false));
  }, [session, activeCategory]);

  async function handleAddToCart(itemId: number) {
    if (!session) return;
    if (session.role !== 'customer' || !session.customerId) {
      toast.error('Sign in as a customer to add items to your cart.');
      return;
    }
    setAddingId(itemId);
    try {
      await addItemToCart(session.key, session.customerId, itemId);
      toast.success('Added to cart');
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Could not add item'
      );
    } finally {
      setAddingId(null);
    }
  }

  return (
    <div>
      <PageHeader
        title='Menu'
        description='Browse items by category and add them to your cart.'
      />

      {categories.length > 0 && (
        <div className='mb-6 flex flex-wrap gap-2'>
          <button
            onClick={() => setActiveCategory(null)}
            className={cn(
              'rounded-full border px-3 py-1.5 text-sm font-medium transition-colors',
              activeCategory === null
                ? 'bg-primary text-primary-foreground border-primary'
                : 'hover:bg-accent'
            )}
          >
            All
          </button>
          {categories.map((c) => (
            <button
              key={c.categoryId}
              onClick={() => setActiveCategory(c.categoryName)}
              className={cn(
                'rounded-full border px-3 py-1.5 text-sm font-medium transition-colors',
                activeCategory === c.categoryName
                  ? 'bg-primary text-primary-foreground border-primary'
                  : 'hover:bg-accent'
              )}
            >
              {c.categoryName}
            </button>
          ))}
        </div>
      )}

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading menu…</p>
      ) : items.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No items found.</p>
      ) : (
        <div className='grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3'>
          {items.map((item) => (
            <Card key={item.itemId}>
              <CardContent>
                <h3 className='font-semibold'>{item.itemName}</h3>
                <p className='text-muted-foreground text-xs'>
                  {item.category?.categoryName}
                </p>
                <p className='mt-2 text-lg font-bold'>
                  {formatMoney(item.cost)}
                </p>
              </CardContent>
              <CardFooter>
                <FormButton
                  size='sm'
                  loading={addingId === item.itemId}
                  onClick={() => handleAddToCart(item.itemId)}
                >
                  Add to cart
                </FormButton>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
