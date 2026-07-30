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
import { createCategory, listCategories, deleteCategory, ApiError, type CategorySummary } from '@/lib/api';
import { Trash2 } from 'lucide-react';

export default function AdminCategoriesPage() {
  const session = useSession();
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [newName, setNewName] = useState('');
  const [creating, setCreating] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  function load() {
    if (!session) return;
    setLoading(true);
    listCategories(session.accessToken)
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
      await createCategory(session.accessToken, newName.trim());
      setNewName('');
      toast.success('Category added');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not add category');
    } finally {
      setCreating(false);
    }
  }

  async function handleDelete(category: CategorySummary) {
    if (!session) return;
    if (!confirm(`Delete category "${category.name}"?`)) return;
    setDeletingId(category.id);
    try {
      await deleteCategory(session.accessToken, category.id);
      toast.success('Category deleted');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not delete category');
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div>
      <PageHeader title='Categories' description='Manage the catalogue category tree.' />

      <form onSubmit={handleCreate} className='mb-6 flex items-end gap-2'>
        <FormInput
          id='newCategory'
          label='New category'
          value={newName}
          onChange={(e) => setNewName(e.target.value)}
          placeholder='e.g. Dry Goods'
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
              <TableHead>Name</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className='text-right'>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {categories.map((c) => (
              <TableRow key={c.id}>
                <TableCell>{c.name}</TableCell>
                <TableCell>{c.status}</TableCell>
                <TableCell className='text-right'>
                  <FormButton
                    variant='ghost'
                    size='icon'
                    fullWidth={false}
                    loading={deletingId === c.id}
                    onClick={() => handleDelete(c)}
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
