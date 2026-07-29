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
  addCategory,
  getAllCategories,
  removeCategory,
  FoodyApiError,
  type Category
} from '@/lib/api';
import { Trash2 } from 'lucide-react';

export default function AdminCategoriesPage() {
  const session = useSession();
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [newName, setNewName] = useState('');
  const [creating, setCreating] = useState(false);
  const [removingName, setRemovingName] = useState<string | null>(null);

  function load() {
    if (!session) return;
    setLoading(true);
    getAllCategories(session.key)
      .then(setCategories)
      .catch(() => setCategories([]))
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !newName.trim()) return;
    setCreating(true);
    try {
      await addCategory(session.key, newName.trim());
      setNewName('');
      toast.success('Category added');
      load();
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Could not add category'
      );
    } finally {
      setCreating(false);
    }
  }

  async function handleRemove(categoryName: string) {
    if (!session) return;
    setRemovingName(categoryName);
    try {
      await removeCategory(session.key, categoryName);
      toast.success('Category removed');
      load();
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Could not remove category'
      );
    } finally {
      setRemovingName(null);
    }
  }

  return (
    <div>
      <PageHeader title='Categories' description='Manage menu categories.' />

      <form onSubmit={handleCreate} className='mb-6 flex items-end gap-2'>
        <FormInput
          id='newCategory'
          label='New category'
          value={newName}
          onChange={(e) => setNewName(e.target.value)}
          placeholder='e.g. Beverages'
        />
        <FormButton type='submit' fullWidth={false} loading={creating}>
          Add
        </FormButton>
      </form>

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : categories.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No categories yet.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Name</TableHead>
              <TableHead className='text-right'>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {categories.map((c) => (
              <TableRow key={c.categoryId}>
                <TableCell>{c.categoryId}</TableCell>
                <TableCell>{c.categoryName}</TableCell>
                <TableCell className='text-right'>
                  <FormButton
                    variant='ghost'
                    size='icon'
                    fullWidth={false}
                    loading={removingName === c.categoryName}
                    onClick={() => handleRemove(c.categoryName)}
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
