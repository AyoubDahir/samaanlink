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
import { formatMoney } from '@/lib/utils';
import { useSession } from '@/lib/session';
import {
  addItem,
  getAllCategories,
  getAllItems,
  removeItem,
  FoodyApiError,
  type Category,
  type Item
} from '@/lib/api';
import { Trash2 } from 'lucide-react';

const emptyForm = { itemName: '', quantity: '', cost: '', categoryName: '' };

export default function AdminItemsPage() {
  const session = useSession();
  const [items, setItems] = useState<Item[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState(emptyForm);
  const [creating, setCreating] = useState(false);
  const [removingId, setRemovingId] = useState<number | null>(null);

  function load() {
    if (!session) return;
    setLoading(true);
    Promise.all([getAllItems(session.key), getAllCategories(session.key)])
      .then(([i, c]) => {
        setItems(i);
        setCategories(c);
        setForm((f) => ({
          ...f,
          categoryName: f.categoryName || c[0]?.categoryName || ''
        }));
      })
      .catch(() => {
        setItems([]);
      })
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !form.itemName.trim() || !form.categoryName) return;
    setCreating(true);
    try {
      await addItem(session.key, {
        itemName: form.itemName.trim(),
        quantity: Number(form.quantity) || 0,
        cost: Number(form.cost) || 0,
        categoryName: form.categoryName
      });
      toast.success('Item added');
      setForm((f) => ({ ...emptyForm, categoryName: f.categoryName }));
      load();
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Could not add item'
      );
    } finally {
      setCreating(false);
    }
  }

  async function handleRemove(itemId: number) {
    if (!session) return;
    setRemovingId(itemId);
    try {
      await removeItem(session.key, itemId);
      toast.success('Item removed');
      load();
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Could not remove item'
      );
    } finally {
      setRemovingId(null);
    }
  }

  return (
    <div>
      <PageHeader
        title='Menu items'
        description='Manage items available across the catalog.'
      />

      <form
        onSubmit={handleCreate}
        className='mb-6 grid grid-cols-2 gap-3 sm:grid-cols-4 sm:items-end'
      >
        <FormInput
          id='itemName'
          label='Name'
          value={form.itemName}
          onChange={(e) => setForm((f) => ({ ...f, itemName: e.target.value }))}
        />
        <FormInput
          id='cost'
          label='Cost'
          type='number'
          step='0.01'
          value={form.cost}
          onChange={(e) => setForm((f) => ({ ...f, cost: e.target.value }))}
        />
        <FormInput
          id='quantity'
          label='Quantity'
          type='number'
          value={form.quantity}
          onChange={(e) => setForm((f) => ({ ...f, quantity: e.target.value }))}
        />
        <div className='space-y-2'>
          <label className='text-sm font-medium' htmlFor='categoryName'>
            Category
          </label>
          <select
            id='categoryName'
            className='border-input bg-background h-10 w-full rounded-md border px-3 text-sm'
            value={form.categoryName}
            onChange={(e) =>
              setForm((f) => ({ ...f, categoryName: e.target.value }))
            }
          >
            {categories.map((c) => (
              <option key={c.categoryId} value={c.categoryName}>
                {c.categoryName}
              </option>
            ))}
          </select>
        </div>
        <FormButton
          type='submit'
          fullWidth={false}
          loading={creating}
          disabled={categories.length === 0}
          className='col-span-2 sm:col-span-4'
        >
          Add item
        </FormButton>
        {categories.length === 0 && !loading && (
          <p className='text-muted-foreground col-span-2 text-xs sm:col-span-4'>
            Add a category first before creating items.
          </p>
        )}
      </form>

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : items.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No items yet.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Category</TableHead>
              <TableHead>Quantity</TableHead>
              <TableHead>Cost</TableHead>
              <TableHead className='text-right'>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {items.map((item) => (
              <TableRow key={item.itemId}>
                <TableCell>{item.itemName}</TableCell>
                <TableCell>{item.category?.categoryName}</TableCell>
                <TableCell>{item.quantity}</TableCell>
                <TableCell>{formatMoney(item.cost)}</TableCell>
                <TableCell className='text-right'>
                  <FormButton
                    variant='ghost'
                    size='icon'
                    fullWidth={false}
                    loading={removingId === item.itemId}
                    onClick={() => handleRemove(item.itemId)}
                  >
                    <Trash2 className='text-destructive size-4' />
                  </FormButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
