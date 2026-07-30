'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { PageHeader } from '@/components/shared/PageHeader';
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell
} from '@/components/ui/table';
import { FormButton } from '@/components/ui/form-button';
import { useSession } from '@/lib/session';
import {
  myRestaurant,
  listBranches,
  listDeliveryAddresses,
  listOrdersByRestaurant,
  createOrder,
  ApiError,
  type OrderSummary
} from '@/lib/api';

export default function RestaurantOrdersPage() {
  const session = useSession();
  const router = useRouter();
  const [restaurantId, setRestaurantId] = useState<string | null>(null);
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);

  function load() {
    if (!session) return;
    setLoading(true);
    myRestaurant(session.accessToken)
      .then((r) => {
        setRestaurantId(r.id);
        return listOrdersByRestaurant(session.accessToken, r.id);
      })
      .then((list) => setOrders([...list].sort((a, b) => b.createdAt.localeCompare(a.createdAt))))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  async function handleNewOrder() {
    if (!session || !restaurantId) return;
    setCreating(true);
    try {
      const branches = await listBranches(session.accessToken, restaurantId);
      const primaryBranch = branches.find((b) => b.primary) ?? branches[0];
      if (!primaryBranch) throw new ApiError('No branch found for your restaurant');

      const addresses = await listDeliveryAddresses(session.accessToken, primaryBranch.id);
      const address = addresses.find((a) => a.defaultAddress) ?? addresses[0];
      if (!address) {
        toast.error('Add a delivery address first');
        router.push('/restaurant/addresses');
        return;
      }

      const order = await createOrder(session.accessToken, restaurantId, address.id);
      router.push(`/restaurant/orders/${order.id}`);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not start a new order');
    } finally {
      setCreating(false);
    }
  }

  return (
    <div>
      <PageHeader
        title='Orders'
        description='Your procurement orders against the platform catalogue.'
        actions={
          <FormButton fullWidth={false} loading={creating} onClick={handleNewOrder}>
            New order
          </FormButton>
        }
      />

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : orders.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No orders yet.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Status</TableHead>
              <TableHead>Lines</TableHead>
              <TableHead>Total</TableHead>
              <TableHead>Created</TableHead>
              <TableHead />
            </TableRow>
          </TableHeader>
          <TableBody>
            {orders.map((o) => (
              <TableRow key={o.id}>
                <TableCell>{o.status}</TableCell>
                <TableCell>{o.lines.length}</TableCell>
                <TableCell>{o.orderTotal ?? '—'}</TableCell>
                <TableCell>{new Date(o.createdAt).toLocaleString()}</TableCell>
                <TableCell className='text-right'>
                  <Link href={`/restaurant/orders/${o.id}`} className='text-primary text-sm font-medium'>
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
