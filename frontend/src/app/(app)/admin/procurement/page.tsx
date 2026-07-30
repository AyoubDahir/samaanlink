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
  listAllPurchaseOrders,
  listSuppliers,
  createPurchaseOrder,
  ApiError,
  type PurchaseOrderSummary,
  type SupplierSummary
} from '@/lib/api';

export default function AdminProcurementPage() {
  const session = useSession();
  const router = useRouter();
  const [orders, setOrders] = useState<PurchaseOrderSummary[]>([]);
  const [suppliers, setSuppliers] = useState<SupplierSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [supplierId, setSupplierId] = useState('');
  const [creating, setCreating] = useState(false);

  function load() {
    if (!session) return;
    setLoading(true);
    Promise.all([listAllPurchaseOrders(session.accessToken), listSuppliers(session.accessToken)])
      .then(([o, s]) => {
        setOrders([...o].sort((a, b) => b.createdAt.localeCompare(a.createdAt)));
        setSuppliers(s.filter((x) => x.status === 'ACTIVE'));
      })
      .catch(() => {
        setOrders([]);
        setSuppliers([]);
      })
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  async function handleCreate() {
    if (!session || !supplierId) {
      toast.error('Pick a supplier first');
      return;
    }
    setCreating(true);
    try {
      const po = await createPurchaseOrder(session.accessToken, supplierId);
      router.push(`/admin/procurement/${po.id}`);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not create purchase order');
    } finally {
      setCreating(false);
    }
  }

  const supplierName = (id: string) => suppliers.find((s) => s.id === id)?.name ?? id;

  return (
    <div>
      <PageHeader
        title='Procurement'
        description='Purchase orders raised against wholesalers/suppliers.'
        actions={
          <div className='flex items-center gap-2'>
            <select
              className='border-input bg-background h-10 rounded-md border px-3 text-sm'
              value={supplierId}
              onChange={(e) => setSupplierId(e.target.value)}
            >
              <option value=''>Select supplier…</option>
              {suppliers.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
            <FormButton fullWidth={false} loading={creating} onClick={handleCreate}>
              New purchase order
            </FormButton>
          </div>
        }
      />

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : orders.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No purchase orders yet.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Supplier</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Subtotal</TableHead>
              <TableHead>Created</TableHead>
              <TableHead />
            </TableRow>
          </TableHeader>
          <TableBody>
            {orders.map((o) => (
              <TableRow key={o.id}>
                <TableCell>{supplierName(o.supplierId)}</TableCell>
                <TableCell>{o.status}</TableCell>
                <TableCell>{o.subtotal ?? '—'}</TableCell>
                <TableCell>{new Date(o.createdAt).toLocaleString()}</TableCell>
                <TableCell className='text-right'>
                  <Link href={`/admin/procurement/${o.id}`} className='text-primary text-sm font-medium'>
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
