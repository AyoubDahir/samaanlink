'use client';

import { useEffect, useState } from 'react';
import { toast } from 'sonner';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { FormInput } from '@/components/ui/form-input';
import { FormButton } from '@/components/ui/form-button';
import { useSession } from '@/lib/session';
import {
  addRestaurant,
  getAllRestaurants,
  removeRestaurant,
  FoodyApiError,
  type Restaurant
} from '@/lib/api';
import { Trash2 } from 'lucide-react';

const emptyForm = {
  restaurantName: '',
  managerName: '',
  contactNunber: '',
  city: '',
  streetNo: '',
  area: '',
  state: '',
  country: '',
  pincode: ''
};

export default function AdminRestaurantsPage() {
  const session = useSession();
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState(emptyForm);
  const [creating, setCreating] = useState(false);
  const [removingId, setRemovingId] = useState<number | null>(null);

  function load() {
    if (!session) return;
    setLoading(true);
    getAllRestaurants(session.key)
      .then(setRestaurants)
      .catch(() => setRestaurants([]))
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  function update(key: keyof typeof form) {
    return (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm((f) => ({ ...f, [key]: e.target.value }));
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !form.restaurantName.trim()) return;
    setCreating(true);
    try {
      await addRestaurant(session.key, {
        restaurantName: form.restaurantName.trim(),
        managerName: form.managerName || undefined,
        contactNunber: form.contactNunber || undefined,
        address: {
          streetNo: form.streetNo || undefined,
          area: form.area || undefined,
          city: form.city || undefined,
          state: form.state || undefined,
          country: form.country || undefined,
          pincode: form.pincode || undefined
        }
      });
      toast.success('Restaurant added');
      setForm(emptyForm);
      load();
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Could not add restaurant'
      );
    } finally {
      setCreating(false);
    }
  }

  async function handleRemove(restaurantId: number) {
    if (!session) return;
    setRemovingId(restaurantId);
    try {
      await removeRestaurant(session.key, restaurantId);
      toast.success('Restaurant removed');
      load();
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError
          ? err.message
          : 'Could not remove restaurant'
      );
    } finally {
      setRemovingId(null);
    }
  }

  return (
    <div>
      <PageHeader
        title='Restaurants'
        description='Manage the restaurant directory.'
      />

      <form
        onSubmit={handleCreate}
        className='mb-6 space-y-3 rounded-lg border p-4'
      >
        <div className='grid grid-cols-1 gap-3 sm:grid-cols-3'>
          <FormInput
            id='restaurantName'
            label='Restaurant name'
            required
            value={form.restaurantName}
            onChange={update('restaurantName')}
          />
          <FormInput
            id='managerName'
            label='Manager'
            value={form.managerName}
            onChange={update('managerName')}
          />
          <FormInput
            id='contactNunber'
            label='Contact number'
            description='10 digits'
            value={form.contactNunber}
            onChange={update('contactNunber')}
          />
        </div>
        <div className='grid grid-cols-2 gap-3 sm:grid-cols-5'>
          <FormInput
            id='streetNo'
            label='Street'
            value={form.streetNo}
            onChange={update('streetNo')}
          />
          <FormInput
            id='area'
            label='Area'
            value={form.area}
            onChange={update('area')}
          />
          <FormInput
            id='city'
            label='City'
            value={form.city}
            onChange={update('city')}
          />
          <FormInput
            id='state'
            label='State'
            value={form.state}
            onChange={update('state')}
          />
          <FormInput
            id='country'
            label='Country'
            value={form.country}
            onChange={update('country')}
          />
        </div>
        <FormButton type='submit' fullWidth={false} loading={creating}>
          Add restaurant
        </FormButton>
      </form>

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : restaurants.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No restaurants yet.</p>
      ) : (
        <div className='grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3'>
          {restaurants.map((r) => (
            <Card key={r.restaurantId}>
              <CardHeader>
                <CardTitle className='flex items-center justify-between text-base'>
                  {r.restaurantName}
                  <FormButton
                    variant='ghost'
                    size='icon'
                    fullWidth={false}
                    loading={removingId === r.restaurantId}
                    onClick={() => handleRemove(r.restaurantId)}
                  >
                    <Trash2 className='text-destructive size-4' />
                  </FormButton>
                </CardTitle>
              </CardHeader>
              <CardContent className='text-muted-foreground text-sm'>
                {r.address?.city && <p>{r.address.city}</p>}
                {r.contactNunber && <p>{r.contactNunber}</p>}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
