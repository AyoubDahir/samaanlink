'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
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
import { listAllOrders, listRestaurants, type OrderSummary, type RestaurantSummary } from '@/lib/api';

export default function AdminOrdersPage() {
  const session = useSession();
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [restaurants, setRestaurants] = useState<RestaurantSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!session) return;
    setLoading(true);
    Promise.all([listAllOrders(session.accessToken), listRestaurants(session.accessToken)])
      .then(([o, r]) => {
        setOrders([...o].sort((a, b) => b.createdAt.localeCompare(a.createdAt)));
        setRestaurants(r);
      })
      .catch(() => {
        setOrders([]);
        setRestaurants([]);
      })
      .finally(() => setLoading(false));
  }, [session]);

  const restaurantName = (id: string) => restaurants.find((r) => r.id === id)?.name ?? id;

  return (
    <div>
      <PageHeader title='Orders' description='All restaurant orders across the platform.' />

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : orders.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No orders yet.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Restaurant</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Total</TableHead>
              <TableHead>Created</TableHead>
              <TableHead />
            </TableRow>
          </TableHeader>
          <TableBody>
            {orders.map((o) => (
              <TableRow key={o.id}>
                <TableCell>{restaurantName(o.restaurantId)}</TableCell>
                <TableCell>{o.status}</TableCell>
                <TableCell>{o.orderTotal ?? '—'}</TableCell>
                <TableCell>{new Date(o.createdAt).toLocaleString()}</TableCell>
                <TableCell className='text-right'>
                  <Link href={`/admin/orders/${o.id}`} className='text-primary text-sm font-medium'>
                    View
                  </Link>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
