'use client';

import { useEffect, useState } from 'react';
import { toast } from 'sonner';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { formatMoney } from '@/lib/utils';
import { useSession } from '@/lib/session';
import {
  getOrderHistoryByCustomer,
  FoodyApiError,
  type OrderHistory
} from '@/lib/api';

export default function OrdersPage() {
  const session = useSession();
  const [history, setHistory] = useState<OrderHistory[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!session?.customerId) return;
    getOrderHistoryByCustomer(session.key, session.customerId)
      .then(setHistory)
      .catch((err) => {
        setHistory([]);
        if (
          err instanceof FoodyApiError &&
          !err.message.toLowerCase().includes('not found')
        ) {
          toast.error(err.message);
        }
      })
      .finally(() => setLoading(false));
  }, [session]);

  return (
    <div>
      <PageHeader
        title='Order history'
        description='Completed orders and their receipts. Just placed an order? Find it via the cart page — it appears here once billed.'
      />

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : history.length === 0 ? (
        <p className='text-muted-foreground text-sm'>
          No completed orders yet.
        </p>
      ) : (
        <div className='space-y-3'>
          {history.map((h) => (
            <Card key={h.orderHistoryId}>
              <CardHeader>
                <CardTitle className='flex items-center justify-between text-base'>
                  <span>Order #{h.bill.order.orderId}</span>
                  <span>{formatMoney(h.bill.totalCost)}</span>
                </CardTitle>
              </CardHeader>
              <CardContent className='text-muted-foreground space-y-1 text-sm'>
                <p>{new Date(h.bill.billDate).toLocaleString()}</p>
                <p>
                  {h.bill.totalItem} item(s) · Status:{' '}
                  {h.bill.order.orderStatus}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
