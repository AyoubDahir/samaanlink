'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card } from '@/components/ui/card';
import { FormButton } from '@/components/ui/form-button';
import { useSession } from '@/lib/session';
import {
  listAllOrders,
  listRestaurants,
  listBillsByRestaurant,
  type OrderSummary,
  type RestaurantSummary,
  type BillSummary
} from '@/lib/api';
import { cn } from '@/lib/utils';
import { ChevronRight, Search, Users, TrendingDown, Wallet } from 'lucide-react';

interface RestaurantGroup {
  restaurant: RestaurantSummary;
  orders: OrderSummary[];
  bills: Map<string, BillSummary>;
  paidTotal: number;
  owedTotal: number;
  earliestDate: string;
  latestDate: string;
}

function initials(name: string) {
  const parts = name.trim().split(/\s+/);
  return ((parts[0]?.[0] ?? '') + (parts[1]?.[0] ?? '')).toUpperCase() || '?';
}

export default function AdminOrdersPage() {
  const session = useSession();
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [restaurants, setRestaurants] = useState<RestaurantSummary[]>([]);
  const [billsByRestaurant, setBillsByRestaurant] = useState<Map<string, BillSummary[]>>(new Map());
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [expanded, setExpanded] = useState<Set<string>>(new Set());

  function load() {
    if (!session) return;
    setLoading(true);
    Promise.all([listAllOrders(session.accessToken), listRestaurants(session.accessToken)])
      .then(async ([o, r]) => {
        setOrders(o);
        setRestaurants(r);

        const restaurantIdsWithOrders = Array.from(new Set(o.map((order) => order.restaurantId)));
        const billLists = await Promise.all(
          restaurantIdsWithOrders.map((id) =>
            listBillsByRestaurant(session.accessToken, id).catch(() => [] as BillSummary[])
          )
        );
        const map = new Map<string, BillSummary[]>();
        restaurantIdsWithOrders.forEach((id, i) => map.set(id, billLists[i]));
        setBillsByRestaurant(map);
      })
      .catch(() => {
        setOrders([]);
        setRestaurants([]);
        setBillsByRestaurant(new Map());
      })
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  const groups: RestaurantGroup[] = useMemo(() => {
    const byRestaurant = new Map<string, OrderSummary[]>();
    for (const order of orders) {
      const list = byRestaurant.get(order.restaurantId) ?? [];
      list.push(order);
      byRestaurant.set(order.restaurantId, list);
    }

    const result: RestaurantGroup[] = [];
    byRestaurant.forEach((restaurantOrders, restaurantId) => {
        const restaurant = restaurants.find((r) => r.id === restaurantId);
        if (!restaurant) return;

        const bills = new Map<string, BillSummary>();
        (billsByRestaurant.get(restaurantId) ?? []).forEach((b) => bills.set(b.orderId, b));

        let paidTotal = 0;
        let owedTotal = 0;
        for (const order of restaurantOrders) {
          const bill = bills.get(order.id);
          if (bill?.status === 'PAID') paidTotal += Number(bill.amount);
          else if (bill?.status === 'ISSUED') owedTotal += Number(bill.amount);
        }

        const dates = restaurantOrders.map((o) => o.createdAt).sort();

        result.push({
          restaurant,
          orders: [...restaurantOrders].sort((a, b) => b.createdAt.localeCompare(a.createdAt)),
          bills,
          paidTotal,
          owedTotal,
          earliestDate: dates[0],
          latestDate: dates[dates.length - 1]
        });
    });

    return result
      .filter((g) => g.restaurant.name.toLowerCase().includes(search.toLowerCase()))
      .sort((a, b) => b.latestDate.localeCompare(a.latestDate));
  }, [orders, restaurants, billsByRestaurant, search]);

  const totalOutstanding = groups.reduce((sum, g) => sum + g.owedTotal, 0);
  const totalCollected = groups.reduce((sum, g) => sum + g.paidTotal, 0);
  const totalOrders = orders.length;

  function toggle(restaurantId: string) {
    setExpanded((prev) => {
      const next = new Set(prev);
      if (next.has(restaurantId)) next.delete(restaurantId);
      else next.add(restaurantId);
      return next;
    });
  }

  function expandAll() {
    setExpanded(new Set(groups.map((g) => g.restaurant.id)));
  }

  return (
    <div>
      <PageHeader title='Orders' description='Expand a restaurant to view and manage their orders.' />

      <div className='mb-6 grid grid-cols-1 gap-3 sm:grid-cols-3'>
        <StatTile
          label='Restaurants'
          value={String(groups.length)}
          sublabel={`${totalOrders} total orders`}
          icon={Users}
        />
        <StatTile
          label='Outstanding'
          value={`$${totalOutstanding.toFixed(2)}`}
          sublabel='across all restaurants'
          icon={TrendingDown}
          tone='negative'
        />
        <StatTile
          label='Collected'
          value={`$${totalCollected.toFixed(2)}`}
          sublabel='total paid'
          icon={Wallet}
          tone='positive'
        />
      </div>

      <div className='mb-5 flex items-center gap-2'>
        <div className='relative flex-1'>
          <Search className='text-muted-foreground absolute top-1/2 left-3 size-4 -translate-y-1/2' />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder='Search restaurants…'
            className='border-input bg-background h-10 w-full rounded-md border pr-3 pl-9 text-sm'
          />
        </div>
        <FormButton variant='outline' size='sm' fullWidth={false} onClick={load}>
          Refresh
        </FormButton>
        <FormButton variant='link' size='sm' fullWidth={false} onClick={expandAll}>
          Expand all
        </FormButton>
      </div>

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : groups.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No orders yet.</p>
      ) : (
        <div className='space-y-3'>
          {groups.map((group) => (
            <RestaurantCard
              key={group.restaurant.id}
              group={group}
              isOpen={expanded.has(group.restaurant.id)}
              onToggle={() => toggle(group.restaurant.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function StatTile({
  label,
  value,
  sublabel,
  icon: Icon,
  tone
}: {
  label: string;
  value: string;
  sublabel: string;
  icon: React.ComponentType<{ className?: string }>;
  tone?: 'positive' | 'negative';
}) {
  return (
    <Card className='flex-row items-center justify-between px-6 py-5'>
      <div>
        <p className='text-muted-foreground text-xs font-medium tracking-wide uppercase'>{label}</p>
        <p className='mt-1 text-2xl font-bold'>{value}</p>
        <p className='text-muted-foreground mt-0.5 text-xs'>{sublabel}</p>
      </div>
      <div
        className={cn(
          'flex size-10 shrink-0 items-center justify-center rounded-full',
          tone === 'positive' && 'bg-emerald-500/10 text-emerald-600',
          tone === 'negative' && 'bg-red-500/10 text-red-600',
          !tone && 'bg-primary/10 text-primary'
        )}
      >
        <Icon className='size-5' />
      </div>
    </Card>
  );
}

function RestaurantCard({
  group,
  isOpen,
  onToggle
}: {
  group: RestaurantGroup;
  isOpen: boolean;
  onToggle: () => void;
}) {
  const { restaurant, orders, bills, paidTotal, owedTotal, earliestDate, latestDate } = group;
  const settled = owedTotal === 0;
  const paidBillCount = orders.filter((o) => bills.get(o.id)?.status === 'PAID').length;
  const unpaidBillCount = orders.filter((o) => bills.get(o.id)?.status === 'ISSUED').length;

  return (
    <Card
      className={cn(
        'gap-0 overflow-hidden border-t-4 py-0',
        settled ? 'border-t-emerald-500' : 'border-t-red-500'
      )}
    >
      <button
        onClick={onToggle}
        className='hover:bg-accent/50 flex w-full items-center gap-3 px-4 py-3.5 text-left transition-colors sm:px-5'
      >
        <ChevronRight className={cn('text-muted-foreground size-4 shrink-0 transition-transform', isOpen && 'rotate-90')} />
        <div className='bg-primary text-primary-foreground flex size-9 shrink-0 items-center justify-center rounded-full text-xs font-semibold'>
          {initials(restaurant.name)}
        </div>
        <div className='min-w-0 flex-1'>
          <div className='flex flex-wrap items-center gap-2'>
            <p className='truncate text-sm font-semibold tracking-wide uppercase'>{restaurant.name}</p>
            <span className='text-muted-foreground bg-muted rounded-full px-2 py-0.5 text-xs'>
              {restaurant.status}
            </span>
          </div>
          <div className='mt-1 flex flex-wrap items-center gap-2 text-xs'>
            <span className='text-muted-foreground'>
              {orders.length} {orders.length === 1 ? 'order' : 'orders'}
            </span>
            {paidBillCount > 0 && (
              <span className='rounded-full bg-emerald-500/10 px-2 py-0.5 font-medium text-emerald-700 dark:text-emerald-400'>
                {paidBillCount} paid
              </span>
            )}
            {unpaidBillCount > 0 && (
              <span className='rounded-full bg-red-500/10 px-2 py-0.5 font-medium text-red-700 dark:text-red-400'>
                {unpaidBillCount} unpaid
              </span>
            )}
            {paidTotal > 0 && (
              <span className='rounded-full bg-emerald-600 px-2 py-0.5 font-semibold text-white'>
                ${paidTotal.toFixed(2)}
              </span>
            )}
            {owedTotal > 0 && (
              <span className='rounded-full bg-red-600 px-2 py-0.5 font-semibold text-white'>
                ${owedTotal.toFixed(2)}
              </span>
            )}
            <span className='text-muted-foreground'>
              · {new Date(earliestDate).toLocaleDateString()} → {new Date(latestDate).toLocaleDateString()}
            </span>
          </div>
        </div>
        <span
          className={cn(
            'shrink-0 rounded-full px-3 py-1 text-xs font-semibold whitespace-nowrap',
            settled
              ? 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-400'
              : 'bg-red-500/10 text-red-700 dark:text-red-400'
          )}
        >
          {settled ? 'Settled' : `Owes $${owedTotal.toFixed(2)}`}
        </span>
      </button>

      {isOpen && (
        <div className='divide-y border-t'>
          {orders.map((order) => {
            const bill = bills.get(order.id);
            return (
              <Link
                key={order.id}
                href={`/admin/orders/${order.id}`}
                className='hover:bg-accent/50 flex items-center justify-between gap-3 px-4 py-2.5 text-sm transition-colors sm:px-5'
              >
                <span className='font-medium'>Order {order.id.slice(0, 8)}</span>
                <span className='text-muted-foreground'>{order.status}</span>
                <span className='text-muted-foreground'>
                  {bill ? `Bill: ${bill.status}` : 'Not billed'}
                </span>
                <span className='text-muted-foreground'>
                  {new Date(order.createdAt).toLocaleDateString()}
                </span>
                <span className='font-semibold'>{order.orderTotal ?? '—'}</span>
              </Link>
            );
          })}
        </div>
      )}
    </Card>
  );
}
