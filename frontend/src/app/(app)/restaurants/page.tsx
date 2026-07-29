'use client';

import { useEffect, useState } from 'react';
import { toast } from 'sonner';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { FormInput } from '@/components/ui/form-input';
import { FormButton } from '@/components/ui/form-button';
import { useSession } from '@/lib/session';
import {
  getAllRestaurants,
  findRestaurantsByCity,
  findRestaurantsByItemName,
  FoodyApiError,
  type Restaurant
} from '@/lib/api';
import { Search, MapPin, Phone } from 'lucide-react';

export default function RestaurantsPage() {
  const session = useSession();
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [city, setCity] = useState('');
  const [itemName, setItemName] = useState('');

  function loadAll() {
    if (!session) return;
    setLoading(true);
    getAllRestaurants(session.key)
      .then(setRestaurants)
      .catch((err) => {
        setRestaurants([]);
        if (err instanceof FoodyApiError) toast.error(err.message);
      })
      .finally(() => setLoading(false));
  }

  useEffect(loadAll, [session]);

  async function handleSearchByCity(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !city.trim()) return;
    setLoading(true);
    try {
      setRestaurants(await findRestaurantsByCity(session.key, city.trim()));
    } catch (err) {
      toast.error(err instanceof FoodyApiError ? err.message : 'Search failed');
    } finally {
      setLoading(false);
    }
  }

  async function handleSearchByItem(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !itemName.trim()) return;
    setLoading(true);
    try {
      setRestaurants(
        await findRestaurantsByItemName(session.key, itemName.trim())
      );
    } catch (err) {
      toast.error(err instanceof FoodyApiError ? err.message : 'Search failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        title='Restaurants'
        description='Browse restaurants, or search by city or menu item.'
      />

      <div className='mb-6 grid grid-cols-1 gap-3 sm:grid-cols-2'>
        <form onSubmit={handleSearchByCity} className='flex items-end gap-2'>
          <FormInput
            id='city'
            label='Search by city'
            value={city}
            onChange={(e) => setCity(e.target.value)}
            placeholder='e.g. Mogadishu'
          />
          <FormButton
            type='submit'
            fullWidth={false}
            size='default'
            icon={<Search className='size-4' />}
          >
            Go
          </FormButton>
        </form>
        <form onSubmit={handleSearchByItem} className='flex items-end gap-2'>
          <FormInput
            id='itemName'
            label='Search by item name'
            value={itemName}
            onChange={(e) => setItemName(e.target.value)}
            placeholder='e.g. Pizza'
          />
          <FormButton
            type='submit'
            fullWidth={false}
            size='default'
            icon={<Search className='size-4' />}
          >
            Go
          </FormButton>
        </form>
      </div>

      {restaurants.length > 0 && (
        <FormButton
          variant='ghost'
          size='sm'
          fullWidth={false}
          onClick={loadAll}
          className='mb-4'
        >
          Clear search
        </FormButton>
      )}

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading restaurants…</p>
      ) : restaurants.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No restaurants found.</p>
      ) : (
        <div className='grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3'>
          {restaurants.map((r) => (
            <Card key={r.restaurantId}>
              <CardHeader>
                <CardTitle>{r.restaurantName}</CardTitle>
              </CardHeader>
              <CardContent className='space-y-1 text-sm'>
                {r.address && (
                  <p className='text-muted-foreground flex items-start gap-1.5'>
                    <MapPin className='mt-0.5 size-3.5 shrink-0' />
                    {[
                      r.address.streetNo,
                      r.address.area,
                      r.address.city,
                      r.address.state,
                      r.address.country
                    ]
                      .filter(Boolean)
                      .join(', ')}
                  </p>
                )}
                {r.contactNunber && (
                  <p className='text-muted-foreground flex items-center gap-1.5'>
                    <Phone className='size-3.5 shrink-0' />
                    {r.contactNunber}
                  </p>
                )}
                {r.managerName && (
                  <p className='text-muted-foreground'>
                    Manager: {r.managerName}
                  </p>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
