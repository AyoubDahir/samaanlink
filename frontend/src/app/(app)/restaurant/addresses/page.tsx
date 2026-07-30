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
import { useSession } from '@/lib/session';
import {
  myRestaurant,
  listBranches,
  listDeliveryAddresses,
  addDeliveryAddress,
  ApiError,
  type BranchSummary,
  type DeliveryAddressSummary
} from '@/lib/api';

export default function RestaurantAddressesPage() {
  const session = useSession();
  const [primaryBranch, setPrimaryBranch] = useState<BranchSummary | null>(null);
  const [addresses, setAddresses] = useState<DeliveryAddressSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ label: '', addressLine: '', city: '' });
  const [creating, setCreating] = useState(false);

  function load() {
    if (!session) return;
    setLoading(true);
    myRestaurant(session.accessToken)
      .then((r) => listBranches(session.accessToken, r.id))
      .then((branches) => {
        const primary = branches.find((b) => b.primary) ?? branches[0] ?? null;
        setPrimaryBranch(primary);
        return primary ? listDeliveryAddresses(session.accessToken, primary.id) : [];
      })
      .then(setAddresses)
      .catch(() => {
        setPrimaryBranch(null);
        setAddresses([]);
      })
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !primaryBranch || !form.label.trim() || !form.addressLine.trim() || !form.city.trim()) return;
    setCreating(true);
    try {
      await addDeliveryAddress(session.accessToken, primaryBranch.id, {
        label: form.label.trim(),
        addressLine: form.addressLine.trim(),
        city: form.city.trim(),
        defaultAddress: addresses.length === 0
      });
      toast.success('Address added');
      setForm({ label: '', addressLine: '', city: '' });
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not add address');
    } finally {
      setCreating(false);
    }
  }

  return (
    <div>
      <PageHeader
        title='Delivery addresses'
        description='Manage where orders for your primary branch get delivered.'
      />

      {!loading && !primaryBranch ? (
        <p className='text-muted-foreground text-sm'>
          No branch found for your account yet — contact your platform admin.
        </p>
      ) : (
        <>
          <form onSubmit={handleCreate} className='mb-6 grid grid-cols-2 gap-3 sm:grid-cols-4'>
            <FormInput
              id='label'
              label='Label'
              placeholder='e.g. Main Kitchen'
              value={form.label}
              onChange={(e) => setForm({ ...form, label: e.target.value })}
            />
            <FormInput
              id='addressLine'
              label='Address'
              value={form.addressLine}
              onChange={(e) => setForm({ ...form, addressLine: e.target.value })}
            />
            <FormInput
              id='city'
              label='City'
              value={form.city}
              onChange={(e) => setForm({ ...form, city: e.target.value })}
            />
            <FormButton type='submit' loading={creating} className='self-end'>
              Add address
            </FormButton>
          </form>

          {loading ? (
            <p className='text-muted-foreground text-sm'>Loading…</p>
          ) : addresses.length === 0 ? (
            <p className='text-muted-foreground text-sm'>No delivery addresses yet.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Label</TableHead>
                  <TableHead>Address</TableHead>
                  <TableHead>City</TableHead>
                  <TableHead>Default</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {addresses.map((a) => (
                  <TableRow key={a.id}>
                    <TableCell>{a.label}</TableCell>
                    <TableCell>{a.addressLine}</TableCell>
                    <TableCell>{a.city}</TableCell>
                    <TableCell>{a.defaultAddress ? 'Yes' : ''}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </>
      )}
    </div>
  );
}
