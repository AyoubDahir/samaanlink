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
  listRestaurants,
  registerRestaurant,
  activateRestaurant,
  suspendRestaurant,
  ApiError,
  type RestaurantSummary
} from '@/lib/api';

const emptyForm = {
  restaurantName: '',
  creditLimit: '0',
  paymentTermDays: '14',
  primaryBranchName: '',
  primaryBranchCity: '',
  ownerEmail: '',
  ownerPassword: '',
  ownerFirstName: '',
  ownerLastName: ''
};

export default function AdminRestaurantsPage() {
  const session = useSession();
  const [restaurants, setRestaurants] = useState<RestaurantSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState(emptyForm);
  const [creating, setCreating] = useState(false);
  const [busyId, setBusyId] = useState<string | null>(null);

  function load() {
    if (!session) return;
    setLoading(true);
    listRestaurants(session.accessToken)
      .then(setRestaurants)
      .catch(() => setRestaurants([]))
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    if (!session) return;
    if (
      !form.restaurantName.trim() ||
      !form.primaryBranchName.trim() ||
      !form.primaryBranchCity.trim() ||
      !form.ownerEmail.trim() ||
      !form.ownerPassword.trim()
    ) {
      toast.error('Please fill in all required fields');
      return;
    }
    setCreating(true);
    try {
      await registerRestaurant(session.accessToken, {
        restaurantName: form.restaurantName.trim(),
        creditLimit: Number(form.creditLimit) || 0,
        paymentTermDays: Number(form.paymentTermDays) || 0,
        primaryBranchName: form.primaryBranchName.trim(),
        primaryBranchCity: form.primaryBranchCity.trim(),
        ownerEmail: form.ownerEmail.trim(),
        ownerPassword: form.ownerPassword,
        ownerFirstName: form.ownerFirstName.trim(),
        ownerLastName: form.ownerLastName.trim()
      });
      toast.success('Restaurant registered (inactive until activated)');
      setForm(emptyForm);
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not register restaurant');
    } finally {
      setCreating(false);
    }
  }

  async function handleActivate(id: string) {
    if (!session) return;
    setBusyId(id);
    try {
      await activateRestaurant(session.accessToken, id);
      toast.success('Restaurant activated');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not activate restaurant');
    } finally {
      setBusyId(null);
    }
  }

  async function handleSuspend(id: string) {
    if (!session) return;
    setBusyId(id);
    try {
      await suspendRestaurant(session.accessToken, id);
      toast.success('Restaurant suspended');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not suspend restaurant');
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div>
      <PageHeader title='Restaurants' description='Onboard and manage restaurant accounts.' />

      <Card className='mb-6'>
        <CardHeader>
          <CardTitle className='text-base'>Register a new restaurant</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleRegister} className='grid grid-cols-2 gap-3 md:grid-cols-4'>
            <FormInput
              id='restaurantName'
              label='Restaurant name'
              value={form.restaurantName}
              onChange={(e) => setForm({ ...form, restaurantName: e.target.value })}
            />
            <FormInput
              id='creditLimit'
              label='Credit limit'
              value={form.creditLimit}
              onChange={(e) => setForm({ ...form, creditLimit: e.target.value })}
            />
            <FormInput
              id='paymentTermDays'
              label='Payment term (days)'
              value={form.paymentTermDays}
              onChange={(e) => setForm({ ...form, paymentTermDays: e.target.value })}
            />
            <FormInput
              id='primaryBranchName'
              label='Primary branch name'
              value={form.primaryBranchName}
              onChange={(e) => setForm({ ...form, primaryBranchName: e.target.value })}
            />
            <FormInput
              id='primaryBranchCity'
              label='City'
              value={form.primaryBranchCity}
              onChange={(e) => setForm({ ...form, primaryBranchCity: e.target.value })}
            />
            <FormInput
              id='ownerEmail'
              label='Owner email'
              type='email'
              value={form.ownerEmail}
              onChange={(e) => setForm({ ...form, ownerEmail: e.target.value })}
            />
            <FormInput
              id='ownerPassword'
              label='Owner password'
              type='password'
              value={form.ownerPassword}
              onChange={(e) => setForm({ ...form, ownerPassword: e.target.value })}
            />
            <FormInput
              id='ownerFirstName'
              label='Owner first name'
              value={form.ownerFirstName}
              onChange={(e) => setForm({ ...form, ownerFirstName: e.target.value })}
            />
            <FormInput
              id='ownerLastName'
              label='Owner last name'
              value={form.ownerLastName}
              onChange={(e) => setForm({ ...form, ownerLastName: e.target.value })}
            />
            <FormButton type='submit' loading={creating} className='self-end md:col-span-4'>
              Register restaurant
            </FormButton>
          </form>
        </CardContent>
      </Card>

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : restaurants.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No restaurants yet.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Credit limit</TableHead>
              <TableHead>Payment terms</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className='text-right'>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {restaurants.map((r) => (
              <TableRow key={r.id}>
                <TableCell>{r.name}</TableCell>
                <TableCell>{r.creditLimit}</TableCell>
                <TableCell>{r.paymentTermDays} days</TableCell>
                <TableCell>{r.status}</TableCell>
                <TableCell className='text-right'>
                  {r.status === 'ACTIVE' ? (
                    <FormButton
                      variant='ghost'
                      size='sm'
                      fullWidth={false}
                      loading={busyId === r.id}
                      onClick={() => handleSuspend(r.id)}
                    >
                      Suspend
                    </FormButton>
                  ) : (
                    <FormButton
                      variant='ghost'
                      size='sm'
                      fullWidth={false}
                      loading={busyId === r.id}
                      onClick={() => handleActivate(r.id)}
                    >
                      Activate
                    </FormButton>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
