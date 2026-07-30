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
  findPurchaseOrder,
  addPurchaseOrderLine,
  removePurchaseOrderLine,
  placePurchaseOrder,
  receivePurchaseOrder,
  cancelPurchaseOrder,
  listProducts,
  listSupplierProductIds,
  ApiError,
  type PurchaseOrderSummary,
  type ProductSummary
} from '@/lib/api';
import { Trash2 } from 'lucide-react';

export default function AdminPurchaseOrderDetailPage() {
  const session = useSession();
  const params = useParams<{ id: string }>();
  const purchaseOrderId = params.id;

  const [po, setPo] = useState<PurchaseOrderSummary | null>(null);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  const [productId, setProductId] = useState('');
  const [quantity, setQuantity] = useState('1');
  const [unitCost, setUnitCost] = useState('');
  const [addingLine, setAddingLine] = useState(false);

  const load = useCallback(() => {
    if (!session) return;
    setLoading(true);
    findPurchaseOrder(session.accessToken, purchaseOrderId)
      .then(async (order) => {
        setPo(order);
        const productIds = await listSupplierProductIds(session.accessToken, order.supplierId);
        const allProducts = await listProducts(session.accessToken);
        setProducts(allProducts.filter((p) => productIds.includes(p.id)));
      })
      .catch(() => setPo(null))
      .finally(() => setLoading(false));
  }, [session, purchaseOrderId]);

  useEffect(load, [load]);

  async function handleAddLine(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !productId || !quantity || !unitCost) return;
    setAddingLine(true);
    try {
      await addPurchaseOrderLine(session.accessToken, purchaseOrderId, productId, Number(quantity), Number(unitCost));
      toast.success('Line added');
      setProductId('');
      setQuantity('1');
      setUnitCost('');
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
      await removePurchaseOrderLine(session.accessToken, purchaseOrderId, lineId);
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
      await placePurchaseOrder(session.accessToken, purchaseOrderId);
      toast.success('Purchase order placed');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not place purchase order');
    } finally {
      setBusy(false);
    }
  }

  async function handleReceive() {
    if (!session) return;
    setBusy(true);
    try {
      await receivePurchaseOrder(session.accessToken, purchaseOrderId);
      toast.success('Goods received');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not mark goods received');
    } finally {
      setBusy(false);
    }
  }

  async function handleCancel() {
    if (!session) return;
    setBusy(true);
    try {
      await cancelPurchaseOrder(session.accessToken, purchaseOrderId);
      toast.success('Purchase order cancelled');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not cancel purchase order');
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <p className='text-muted-foreground text-sm'>Loading…</p>;
  if (!po) return <p className='text-muted-foreground text-sm'>Purchase order not found.</p>;

  const productName = (id: string) => products.find((p) => p.id === id)?.name ?? id;
  const isDraft = po.status === 'DRAFT';

  return (
    <div>
      <PageHeader
        title={`Purchase order ${po.id.slice(0, 8)}`}
        description={`Status: ${po.status}`}
        actions={
          isDraft ? (
            <div className='flex gap-2'>
              <FormButton variant='outline' fullWidth={false} loading={busy} onClick={handleCancel}>
                Cancel
              </FormButton>
              <FormButton fullWidth={false} loading={busy} disabled={po.lines.length === 0} onClick={handlePlace}>
                Place order
              </FormButton>
            </div>
          ) : po.status === 'PLACED' ? (
            <div className='flex gap-2'>
              <FormButton variant='outline' fullWidth={false} loading={busy} onClick={handleCancel}>
                Cancel
              </FormButton>
              <FormButton fullWidth={false} loading={busy} onClick={handleReceive}>
                Mark goods received
              </FormButton>
            </div>
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
          <div className='space-y-2'>
            <label className='text-sm font-medium' htmlFor='unitCost'>
              Unit cost
            </label>
            <input
              id='unitCost'
              className='border-input bg-background h-10 w-24 rounded-md border px-3 text-sm'
              value={unitCost}
              onChange={(e) => setUnitCost(e.target.value)}
            />
          </div>
          <FormButton type='submit' fullWidth={false} loading={addingLine}>
            Add line
          </FormButton>
        </form>
      )}
      {isDraft && products.length === 0 && (
        <p className='text-muted-foreground mb-4 text-sm'>
          This supplier has no linked products yet — link one from the Suppliers page first.
        </p>
      )}

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Product</TableHead>
            <TableHead>Quantity</TableHead>
            <TableHead>Unit cost</TableHead>
            <TableHead>Line total</TableHead>
            {isDraft && <TableHead />}
          </TableRow>
        </TableHeader>
        <TableBody>
          {po.lines.map((line) => (
            <TableRow key={line.id}>
              <TableCell>{productName(line.productId)}</TableCell>
              <TableCell>{line.quantity}</TableCell>
              <TableCell>{line.unitCost}</TableCell>
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

      {po.status !== 'DRAFT' && (
        <Card className='mt-6'>
          <CardHeader>
            <CardTitle className='text-base'>Summary</CardTitle>
          </CardHeader>
          <CardContent className='text-sm'>
            <p className='font-semibold'>Subtotal: {po.subtotal}</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
