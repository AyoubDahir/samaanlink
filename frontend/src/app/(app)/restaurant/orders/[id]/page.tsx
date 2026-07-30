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
  addOrderLine,
  removeOrderLine,
  placeOrder,
  cancelOrder,
  markOrderDelivered,
  listProducts,
  findBillByOrder,
  ApiError,
  type OrderSummary,
  type ProductSummary,
  type BillSummary
} from '@/lib/api';
import { Trash2 } from 'lucide-react';

export default function RestaurantOrderDetailPage() {
  const session = useSession();
  const params = useParams<{ id: string }>();
  const orderId = params.id;

  const [order, setOrder] = useState<OrderSummary | null>(null);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [bill, setBill] = useState<BillSummary | null>(null);
  const [loading, setLoading] = useState(true);

  const [productId, setProductId] = useState('');
  const [quantity, setQuantity] = useState('1');
  const [addingLine, setAddingLine] = useState(false);
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    if (!session) return;
    setLoading(true);
    Promise.all([findOrder(session.accessToken, orderId), listProducts(session.accessToken)])
      .then(async ([o, p]) => {
        setOrder(o);
        setProducts(p.filter((x) => x.status === 'ACTIVE'));
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

  async function handleAddLine(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !productId || !quantity) return;
    setAddingLine(true);
    try {
      await addOrderLine(session.accessToken, orderId, productId, Number(quantity));
      toast.success('Line added');
      setProductId('');
      setQuantity('1');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not add line');
    } finally {
      setAddingLine(false);
    }
  }

  async function handleRemoveLine(lineId: string) {
    if (!session) return;
    setBusy(true);
    try {
      await removeOrderLine(session.accessToken, orderId, lineId);
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not remove line');
    } finally {
      setBusy(false);
    }
  }

  async function handlePlace() {
    if (!session) return;
    setBusy(true);
    try {
      await placeOrder(session.accessToken, orderId);
      toast.success('Order placed');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not place order');
    } finally {
      setBusy(false);
    }
  }

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

  async function handleMarkReceived() {
    if (!session) return;
    setBusy(true);
    try {
      await markOrderDelivered(session.accessToken, orderId);
      toast.success('Order marked as received');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not update order status');
    } finally {
      setBusy(false);
    }
  }

  if (loading) {
    return <p className='text-muted-foreground text-sm'>Loading…</p>;
  }

  if (!order) {
    return <p className='text-muted-foreground text-sm'>Order not found.</p>;
  }

  const productName = (id: string) => products.find((p) => p.id === id)?.name ?? id;
  const isDraft = order.status === 'DRAFT';

  return (
    <div>
      <PageHeader
        title={`Order ${order.id.slice(0, 8)}`}
        description={`Status: ${order.status}`}
        actions={
          isDraft ? (
            <div className='flex gap-2'>
              <FormButton variant='outline' fullWidth={false} loading={busy} onClick={handleCancel}>
                Cancel
              </FormButton>
              <FormButton
                fullWidth={false}
                loading={busy}
                disabled={order.lines.length === 0}
                onClick={handlePlace}
              >
                Place order
              </FormButton>
            </div>
          ) : order.status === 'PLACED' ? (
            <FormButton fullWidth={false} loading={busy} onClick={handleMarkReceived}>
              Confirm received
            </FormButton>
          ) : undefined
        }
      />

      {isDraft && (
        <form onSubmit={handleAddLine} className='mb-6 flex flex-wrap items-end gap-2'>
          <div className='space-y-2'>
            <label className='text-sm font-medium' htmlFor='productId'>
              Product
            </label>
            <select
              id='productId'
              className='border-input bg-background h-10 w-56 rounded-md border px-3 text-sm'
              value={productId}
              onChange={(e) => setProductId(e.target.value)}
            >
              <option value=''>Select…</option>
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.sku})
                </option>
              ))}
            </select>
          </div>
          <div className='space-y-2'>
            <label className='text-sm font-medium' htmlFor='quantity'>
              Quantity
            </label>
            <input
              id='quantity'
              className='border-input bg-background h-10 w-24 rounded-md border px-3 text-sm'
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
            />
          </div>
          <FormButton type='submit' fullWidth={false} loading={addingLine}>
            Add line
          </FormButton>
        </form>
      )}

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Product</TableHead>
            <TableHead>Quantity</TableHead>
            <TableHead>Line total</TableHead>
            {isDraft && <TableHead />}
          </TableRow>
        </TableHeader>
        <TableBody>
          {order.lines.map((line) => (
            <TableRow key={line.id}>
              <TableCell>{productName(line.productId)}</TableCell>
              <TableCell>{line.quantity}</TableCell>
              <TableCell>{line.lineTotal}</TableCell>
              {isDraft && (
                <TableCell className='text-right'>
                  <FormButton
                    variant='ghost'
                    size='icon'
                    fullWidth={false}
                    loading={busy}
                    onClick={() => handleRemoveLine(line.id)}
                  >
                    <Trash2 className='text-destructive size-4' />
                  </FormButton>
                </TableCell>
              )}
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
            <CardTitle className='text-base'>Billing</CardTitle>
          </CardHeader>
          <CardContent className='space-y-1 text-sm'>
            {bill ? (
              <>
                <p>Amount: {bill.amount}</p>
                <p>Status: {bill.status}</p>
              </>
            ) : (
              <p className='text-muted-foreground'>Not billed yet.</p>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
