'use client';

import { useEffect, useState } from 'react';
import { PageHeader } from '@/components/shared/PageHeader';
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell
} from '@/components/ui/table';
import { useSession } from '@/lib/session';
import { listCategories, listProductsByCategory, type CategorySummary, type ProductSummary } from '@/lib/api';
import { cn } from '@/lib/utils';

export default function RestaurantCataloguePage() {
  const session = useSession();
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [activeCategoryId, setActiveCategoryId] = useState<string | null>(null);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!session) return;
    listCategories(session.accessToken)
      .then((cats) => {
        setCategories(cats);
        if (cats.length > 0) setActiveCategoryId(cats[0].id);
      })
      .catch(() => setCategories([]));
  }, [session]);

  useEffect(() => {
    if (!session || !activeCategoryId) return;
    setLoading(true);
    listProductsByCategory(session.accessToken, activeCategoryId)
      .then((p) => setProducts(p.filter((x) => x.status === 'ACTIVE')))
      .catch(() => setProducts([]))
      .finally(() => setLoading(false));
  }, [session, activeCategoryId]);

  return (
    <div>
      <PageHeader
        title='Catalogue'
        description='Browse available products. Add them to an order from the Orders page.'
      />

      {categories.length > 0 && (
        <div className='mb-4 flex flex-wrap gap-2'>
          {categories.map((c) => (
            <button
              key={c.id}
              onClick={() => setActiveCategoryId(c.id)}
              className={cn(
                'rounded-full border px-3 py-1 text-sm',
                activeCategoryId === c.id
                  ? 'bg-accent text-accent-foreground border-accent'
                  : 'text-muted-foreground'
              )}
            >
              {c.name}
            </button>
          ))}
        </div>
      )}

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : products.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No products in this category yet.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>SKU</TableHead>
              <TableHead>Selling unit</TableHead>
              <TableHead>Package size</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {products.map((p) => (
              <TableRow key={p.id}>
                <TableCell>{p.name}</TableCell>
                <TableCell>{p.sku}</TableCell>
                <TableCell>{p.sellingUnitCode}</TableCell>
                <TableCell>{p.packageSize}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
