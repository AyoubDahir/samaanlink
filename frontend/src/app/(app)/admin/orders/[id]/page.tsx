'use client';

import { useEffect, useState, useCallback } from 'react';
import { useParams } from 'next/navigation';
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
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { useSession } from '@/lib/session';
import {
  findOrder,
  cancelOrder,
  listProducts,
  generateBill,
  findBillByOrder,
  payBill,
  ApiError,
  type OrderSummary,
  type ProductSummary,
  type BillSummary
} from '@/lib/api';

export default function AdminOrderDetailPage() {
  const session = useSession();
  const params = useParams<{ id: string }>();
  const orderId = params.id;

  const [order, setOrder] = useState<OrderSummary | null>(null);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [bill, setBill] = useState<BillSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    if (!session) return;
    setLoading(true);
    Promise.all([findOrder(session.accessToken, orderId), listProducts(session.accessToken)])
      .then(async ([o, p]) => {
        setOrder(o);
        setProducts(p);
        if (o.status === 'PLACED' || o.status === 'DELIVERED') {
          try {
            setBill(await findBillByOrder(session.accessToken, orderId));
          } catch {
            setBill(null);
          }
        } else {
          setBill(null);
        }
      })
      .catch(() => setOrder(null))
      .finally(() => setLoading(false));
  }, [session, orderId]);

  useEffect(load, [load]);

  async function handleCancel() {
    if (!session) return;
    setBusy(true);
    try {
      await cancelOrder(session.accessToken, orderId);
      toast.success('Order cancelled');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not cancel order');
    } finally {
      setBusy(false);
    }
  }

  async function handleGenerateBill() {
    if (!session) return;
    setBusy(true);
    try {
      setBill(await generateBill(session.accessToken, orderId));
      toast.success('Bill generated');
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not generate bill');
    } finally {
      setBusy(false);
    }
  }

  async function handleCapturePayment() {
    if (!session || !bill) return;
    setBusy(true);
    try {
      setBill(await payBill(session.accessToken, bill.id));
      toast.success('Payment captured — bill marked paid');
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not mark bill paid');
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <p className='text-muted-foreground text-sm'>Loading…</p>;
  if (!order) return <p className='text-muted-foreground text-sm'>Order not found.</p>;

  const productName = (id: string) => products.find((p) => p.id === id)?.name ?? id;

  return (
    <div>
      <PageHeader
        title={`Order ${order.id.slice(0, 8)}`}
        description={`Status: ${order.status}`}
        actions={
          order.status === 'DRAFT' || order.status === 'PLACED' ? (
            <FormButton variant='outline' fullWidth={false} loading={busy} onClick={handleCancel}>
              Cancel order
            </FormButton>
          ) : undefined
        }
      />

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Product</TableHead>
            <TableHead>Quantity</TableHead>
            <TableHead>Line total</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {order.lines.map((line) => (
            <TableRow key={line.id}>
              <TableCell>{productName(line.productId)}</TableCell>
              <TableCell>{line.quantity}</TableCell>
              <TableCell>{line.lineTotal}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {order.status !== 'DRAFT' && (
        <Card className='mt-6'>
          <CardHeader>
            <CardTitle className='text-base'>Summary</CardTitle>
          </CardHeader>
          <CardContent className='space-y-1 text-sm'>
            <p>Subtotal: {order.subtotal}</p>
            <p>Delivery fee: {order.deliveryFee}</p>
            <p className='font-semibold'>Total: {order.orderTotal}</p>
          </CardContent>
        </Card>
      )}

      {(order.status === 'PLACED' || order.status === 'DELIVERED') && (
        <Card className='mt-6'>
          <CardHeader>
            <CardTitle className='text-base'>Payment</CardTitle>
          </CardHeader>
          <CardContent>
            {bill ? (
              <div className='space-y-1 text-sm'>
                <p>Amount: {bill.amount}</p>
                <p>Status: {bill.status}</p>
                {bill.status === 'ISSUED' && (
                  <FormButton className='mt-2' fullWidth={false} loading={busy} onClick={handleCapturePayment}>
                    Capture payment (mark paid)
                  </FormButton>
                )}
              </div>
            ) : (
              <FormButton fullWidth={false} loading={busy} onClick={handleGenerateBill}>
                Generate bill
              </FormButton>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
