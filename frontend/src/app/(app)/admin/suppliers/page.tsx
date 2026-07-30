'use client';

import { useEffect, useState } from 'react';
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
import { FormInput } from '@/components/ui/form-input';
import { FormButton } from '@/components/ui/form-button';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { useSession } from '@/lib/session';
import {
  listSuppliers,
  registerSupplier,
  activateSupplier,
  suspendSupplier,
  linkSupplierProduct,
  listProducts,
  ApiError,
  type SupplierSummary,
  type ProductSummary
} from '@/lib/api';

export default function AdminSuppliersPage() {
  const session = useSession();
  const [suppliers, setSuppliers] = useState<SupplierSummary[]>([]);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ name: '', leadTimeDays: '3', paymentTermDays: '30' });
  const [creating, setCreating] = useState(false);
  const [busyId, setBusyId] = useState<string | null>(null);

  function load() {
    if (!session) return;
    setLoading(true);
    Promise.all([listSuppliers(session.accessToken), listProducts(session.accessToken)])
      .then(([s, p]) => {
        setSuppliers(s);
        setProducts(p.filter((x) => x.status === 'ACTIVE'));
      })
      .catch(() => {
        setSuppliers([]);
        setProducts([]);
      })
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !form.name.trim()) return;
    setCreating(true);
    try {
      await registerSupplier(session.accessToken, {
        name: form.name.trim(),
        leadTimeDays: Number(form.leadTimeDays) || 0,
        paymentTermDays: Number(form.paymentTermDays) || 0
      });
      toast.success('Supplier registered (inactive until activated)');
      setForm({ name: '', leadTimeDays: '3', paymentTermDays: '30' });
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not register supplier');
    } finally {
      setCreating(false);
    }
  }

  async function handleActivate(id: string) {
    if (!session) return;
    setBusyId(id);
    try {
      await activateSupplier(session.accessToken, id);
      toast.success('Supplier activated');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not activate supplier');
    } finally {
      setBusyId(null);
    }
  }

  async function handleSuspend(id: string) {
    if (!session) return;
    setBusyId(id);
    try {
      await suspendSupplier(session.accessToken, id);
      toast.success('Supplier suspended');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not suspend supplier');
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div>
      <PageHeader title='Suppliers' description='Wholesalers the platform buys products from.' />

      <Card className='mb-6'>
        <CardHeader>
          <CardTitle className='text-base'>Register a new supplier</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleRegister} className='grid grid-cols-2 gap-3 sm:grid-cols-3'>
            <FormInput
              id='name'
              label='Name'
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
            <FormInput
              id='leadTimeDays'
              label='Lead time (days)'
              value={form.leadTimeDays}
              onChange={(e) => setForm({ ...form, leadTimeDays: e.target.value })}
            />
            <FormInput
              id='paymentTermDays'
              label='Payment term (days)'
              value={form.paymentTermDays}
              onChange={(e) => setForm({ ...form, paymentTermDays: e.target.value })}
            />
            <FormButton type='submit' loading={creating} className='self-end'>
              Register
            </FormButton>
          </form>
        </CardContent>
      </Card>

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : suppliers.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No suppliers yet.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Lead time</TableHead>
              <TableHead>Payment terms</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className='text-right'>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {suppliers.map((s) => (
              <SupplierRow
                key={s.id}
                supplier={s}
                products={products}
                token={session!.accessToken}
                busy={busyId === s.id}
                onActivate={() => handleActivate(s.id)}
                onSuspend={() => handleSuspend(s.id)}
              />
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}

function SupplierRow({
  supplier,
  products,
  token,
  busy,
  onActivate,
  onSuspend
}: {
  supplier: SupplierSummary;
  products: ProductSummary[];
  token: string;
  busy: boolean;
  onActivate: () => void;
  onSuspend: () => void;
}) {
  const [productId, setProductId] = useState('');
  const [linking, setLinking] = useState(false);

  async function handleLink() {
    if (!productId) return;
    setLinking(true);
    try {
      await linkSupplierProduct(token, supplier.id, productId);
      toast.success('Product linked to supplier');
      setProductId('');
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not link product');
    } finally {
      setLinking(false);
    }
  }

  return (
    <TableRow>
      <TableCell>{supplier.name}</TableCell>
      <TableCell>{supplier.leadTimeDays}d</TableCell>
      <TableCell>{supplier.paymentTermDays}d</TableCell>
      <TableCell>{supplier.status}</TableCell>
      <TableCell className='text-right'>
        <div className='flex items-center justify-end gap-1'>
          <select
            className='border-input bg-background h-8 rounded-md border px-2 text-xs'
            value={productId}
            onChange={(e) => setProductId(e.target.value)}
          >
            <option value=''>Link product…</option>
            {products.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
          <FormButton variant='outline' size='sm' fullWidth={false} loading={linking} onClick={handleLink}>
            Link
          </FormButton>
          {supplier.status === 'ACTIVE' ? (
            <FormButton variant='ghost' size='sm' fullWidth={false} loading={busy} onClick={onSuspend}>
              Suspend
            </FormButton>
          ) : (
            <FormButton variant='ghost' size='sm' fullWidth={false} loading={busy} onClick={onActivate}>
              Activate
            </FormButton>
          )}
        </div>
      </TableCell>
    </TableRow>
  );
}
