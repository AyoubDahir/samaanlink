'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card, CardContent } from '@/components/ui/card';
import { FormButton } from '@/components/ui/form-button';
import { formatMoney } from '@/lib/utils';
import { useSession } from '@/lib/session';
import {
  getOrder,
  generateBill,
  FoodyApiError,
  type OrderDetails,
  type Bill
} from '@/lib/api';

export default function OrderDetailPage() {
  const params = useParams<{ id: string }>();
  const orderId = Number(params.id);
  const session = useSession();
  const router = useRouter();
  const [order, setOrder] = useState<OrderDetails | null>(null);
  const [bill, setBill] = useState<Bill | null>(null);
  const [loading, setLoading] = useState(true);
  const [billing, setBilling] = useState(false);

  useEffect(() => {
    if (!session || !orderId) return;
    getOrder(session.key, orderId)
      .then(setOrder)
      .catch((err) => {
        if (err instanceof FoodyApiError) toast.error(err.message);
      })
      .finally(() => setLoading(false));
  }, [session, orderId]);

  async function handleGenerateBill() {
    if (!session?.customerId) return;
    setBilling(true);
    try {
      const b = await generateBill(session.key, session.customerId, orderId);
      setBill(b);
      toast.success('Bill generated — order complete!');
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Could not generate bill'
      );
    } finally {
      setBilling(false);
    }
  }

  if (loading)
    return <p className='text-muted-foreground text-sm'>Loading order…</p>;
  if (!order)
    return <p className='text-muted-foreground text-sm'>Order not found.</p>;

  const total = order.foodCart.itemList.reduce(
    (sum, i) => sum + i.cost * i.quantity,
    0
  );
  const isPending = order.orderStatus.toLowerCase().includes('pending');

  return (
    <div>
      <PageHeader
        title={`Order #${order.orderId}`}
        description={`Placed ${new Date(order.orderDate).toLocaleString()} · Status: ${order.orderStatus}`}
      />

      <Card>
        <CardContent className='space-y-2'>
          {order.foodCart.itemList.map((item) => (
            <div key={item.itemId} className='flex justify-between text-sm'>
              <span>
                {item.itemName} × {item.quantity}
              </span>
              <span>{formatMoney(item.cost * item.quantity)}</span>
            </div>
          ))}
          <div className='flex justify-between border-t pt-2 font-bold'>
            <span>Total</span>
            <span>{formatMoney(total)}</span>
          </div>
        </CardContent>
      </Card>

      {bill ? (
        <Card className='mt-4'>
          <CardContent>
            <p className='font-semibold'>Bill #{bill.billId}</p>
            <p className='text-muted-foreground text-sm'>
              {bill.totalItem} item(s) · {formatMoney(bill.totalCost)}
            </p>
            <FormButton
              className='mt-3'
              fullWidth={false}
              onClick={() => router.push('/orders')}
            >
              View order history
            </FormButton>
          </CardContent>
        </Card>
      ) : (
        isPending && (
          <FormButton
            className='mt-4'
            fullWidth={false}
            loading={billing}
            onClick={handleGenerateBill}
          >
            Generate bill (checkout)
          </FormButton>
        )
      )}
    </div>
  );
}
