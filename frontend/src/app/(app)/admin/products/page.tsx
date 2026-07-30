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
  createProduct,
  listProducts,
  listCategories,
  activateProduct,
  discontinueProduct,
  deleteProduct,
  setPurchasePrice,
  setStandardSellingPrice,
  addProductImage,
  ApiError,
  type ProductSummary,
  type CategorySummary
} from '@/lib/api';
import { Package, Trash2 } from 'lucide-react';

const UNITS = ['KG', 'G', 'L', 'PIECE', 'PACK', 'BOX'];

export default function AdminProductsPage() {
  const session = useSession();
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [loading, setLoading] = useState(true);

  const [form, setForm] = useState({
    name: '',
    sku: '',
    categoryId: '',
    purchaseUnitCode: 'KG',
    sellingUnitCode: 'KG',
    packageSize: '1',
    unitsPerPackage: '1',
    imageUrl: ''
  });
  const [creating, setCreating] = useState(false);
  const [busyId, setBusyId] = useState<string | null>(null);

  function load() {
    if (!session) return;
    setLoading(true);
    Promise.all([listProducts(session.accessToken), listCategories(session.accessToken)])
      .then(([p, c]) => {
        setProducts(p);
        setCategories(c);
      })
      .catch(() => {
        setProducts([]);
        setCategories([]);
      })
      .finally(() => setLoading(false));
  }

  useEffect(load, [session]);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!session || !form.name.trim() || !form.sku.trim() || !form.categoryId) {
      toast.error('Name, SKU and category are required');
      return;
    }
    setCreating(true);
    try {
      const product = await createProduct(session.accessToken, {
        name: form.name.trim(),
        sku: form.sku.trim(),
        categoryId: form.categoryId,
        purchaseUnitCode: form.purchaseUnitCode,
        sellingUnitCode: form.sellingUnitCode,
        packageSize: Number(form.packageSize) || 1,
        unitsPerPackage: Number(form.unitsPerPackage) || 1
      });
      if (form.imageUrl.trim()) {
        await addProductImage(session.accessToken, product.id, form.imageUrl.trim());
      }
      toast.success('Product added (inactive until activated)');
      setForm({ ...form, name: '', sku: '', imageUrl: '' });
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not add product');
    } finally {
      setCreating(false);
    }
  }

  async function handleActivate(productId: string) {
    if (!session) return;
    setBusyId(productId);
    try {
      await activateProduct(session.accessToken, productId);
      toast.success('Product activated');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not activate product');
    } finally {
      setBusyId(null);
    }
  }

  async function handleDiscontinue(productId: string) {
    if (!session) return;
    setBusyId(productId);
    try {
      await discontinueProduct(session.accessToken, productId);
      toast.success('Product discontinued');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not discontinue product');
    } finally {
      setBusyId(null);
    }
  }

  async function handleDelete(product: ProductSummary) {
    if (!session) return;
    if (!confirm(`Delete product "${product.name}"? This cannot be undone.`)) return;
    setBusyId(product.id);
    try {
      await deleteProduct(session.accessToken, product.id);
      toast.success('Product deleted');
      load();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not delete product');
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div>
      <PageHeader title='Products' description='Manage the catalogue and set purchase/selling prices.' />

      <form onSubmit={handleCreate} className='mb-6 grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-6'>
        <FormInput
          id='name'
          label='Name'
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
        <FormInput
          id='sku'
          label='SKU'
          value={form.sku}
          onChange={(e) => setForm({ ...form, sku: e.target.value })}
        />
        <div className='space-y-2'>
          <label className='text-sm font-medium' htmlFor='categoryId'>
            Category
          </label>
          <select
            id='categoryId'
            className='border-input bg-background h-10 w-full rounded-md border px-3 text-sm'
            value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
          >
            <option value=''>Select…</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>
        <div className='space-y-2'>
          <label className='text-sm font-medium' htmlFor='purchaseUnit'>
            Purchase unit
          </label>
          <select
            id='purchaseUnit'
            className='border-input bg-background h-10 w-full rounded-md border px-3 text-sm'
            value={form.purchaseUnitCode}
            onChange={(e) => setForm({ ...form, purchaseUnitCode: e.target.value })}
          >
            {UNITS.map((u) => (
              <option key={u} value={u}>
                {u}
              </option>
            ))}
          </select>
        </div>
        <div className='space-y-2'>
          <label className='text-sm font-medium' htmlFor='sellingUnit'>
            Selling unit
          </label>
          <select
            id='sellingUnit'
            className='border-input bg-background h-10 w-full rounded-md border px-3 text-sm'
            value={form.sellingUnitCode}
            onChange={(e) => setForm({ ...form, sellingUnitCode: e.target.value })}
          >
            {UNITS.map((u) => (
              <option key={u} value={u}>
                {u}
              </option>
            ))}
          </select>
        </div>
        <FormInput
          id='imageUrl'
          label='Image URL'
          placeholder='https://…'
          value={form.imageUrl}
          onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
          wrapperClassName='sm:col-span-2 md:col-span-2'
        />
        <FormButton type='submit' loading={creating} className='self-end'>
          Add product
        </FormButton>
      </form>

      {loading ? (
        <p className='text-muted-foreground text-sm'>Loading…</p>
      ) : products.length === 0 ? (
        <p className='text-muted-foreground text-sm'>No products yet.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead />
              <TableHead>Name</TableHead>
              <TableHead>SKU</TableHead>
              <TableHead>Category</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Prices</TableHead>
              <TableHead className='text-right'>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {products.map((p) => (
              <ProductRow
                key={p.id}
                product={p}
                token={session!.accessToken}
                busy={busyId === p.id}
                onActivate={() => handleActivate(p.id)}
                onDiscontinue={() => handleDiscontinue(p.id)}
                onDelete={() => handleDelete(p)}
                onImageChanged={load}
              />
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}

function ProductRow({
  product,
  token,
  busy,
  onActivate,
  onDiscontinue,
  onDelete,
  onImageChanged
}: {
  product: ProductSummary;
  token: string;
  busy: boolean;
  onActivate: () => void;
  onDiscontinue: () => void;
  onDelete: () => void;
  onImageChanged: () => void;
}) {
  const [purchasePrice, setPurchasePriceValue] = useState('');
  const [sellingPrice, setSellingPriceValue] = useState('');
  const [savingPrices, setSavingPrices] = useState(false);
  const [editingImage, setEditingImage] = useState(false);
  const [imageUrlInput, setImageUrlInput] = useState('');
  const [savingImage, setSavingImage] = useState(false);

  async function handleSaveImage() {
    if (!imageUrlInput.trim()) return;
    setSavingImage(true);
    try {
      await addProductImage(token, product.id, imageUrlInput.trim());
      toast.success('Image updated');
      setEditingImage(false);
      setImageUrlInput('');
      onImageChanged();
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not update image');
    } finally {
      setSavingImage(false);
    }
  }

  async function handleSavePrices() {
    if (!purchasePrice && !sellingPrice) return;
    setSavingPrices(true);
    try {
      if (purchasePrice) await setPurchasePrice(token, product.id, Number(purchasePrice));
      if (sellingPrice) await setStandardSellingPrice(token, product.id, Number(sellingPrice));
      toast.success('Prices saved');
      setPurchasePriceValue('');
      setSellingPriceValue('');
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not save prices');
    } finally {
      setSavingPrices(false);
    }
  }

  return (
    <TableRow>
      <TableCell>
        {editingImage ? (
          <div className='flex items-center gap-1'>
            <input
              autoFocus
              className='border-input bg-background h-8 w-32 rounded-md border px-2 text-xs'
              placeholder='Image URL'
              value={imageUrlInput}
              onChange={(e) => setImageUrlInput(e.target.value)}
            />
            <FormButton
              variant='outline'
              size='sm'
              fullWidth={false}
              loading={savingImage}
              onClick={handleSaveImage}
            >
              Save
            </FormButton>
          </div>
        ) : (
          <button
            onClick={() => {
              setImageUrlInput(product.imageUrl ?? '');
              setEditingImage(true);
            }}
            className='bg-muted hover:ring-primary flex size-10 items-center justify-center overflow-hidden rounded-md transition-shadow hover:ring-2'
            title='Set image'
          >
            {product.imageUrl ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img src={product.imageUrl} alt='' className='size-full object-cover' />
            ) : (
              <Package className='text-muted-foreground/50 size-5' />
            )}
          </button>
        )}
      </TableCell>
      <TableCell>{product.name}</TableCell>
      <TableCell>{product.sku}</TableCell>
      <TableCell>{product.categoryName}</TableCell>
      <TableCell>{product.status}</TableCell>
      <TableCell>
        <div className='flex items-center gap-1'>
          <input
            className='border-input bg-background h-8 w-20 rounded-md border px-2 text-xs'
            placeholder='Buy'
            value={purchasePrice}
            onChange={(e) => setPurchasePriceValue(e.target.value)}
          />
          <input
            className='border-input bg-background h-8 w-20 rounded-md border px-2 text-xs'
            placeholder='Sell'
            value={sellingPrice}
            onChange={(e) => setSellingPriceValue(e.target.value)}
          />
          <FormButton
            variant='outline'
            size='sm'
            fullWidth={false}
            loading={savingPrices}
            onClick={handleSavePrices}
          >
            Save
          </FormButton>
        </div>
      </TableCell>
      <TableCell className='text-right'>
        <div className='flex items-center justify-end gap-1'>
          {product.status === 'ACTIVE' ? (
            <FormButton
              variant='ghost'
              size='sm'
              fullWidth={false}
              loading={busy}
              onClick={onDiscontinue}
            >
              Discontinue
            </FormButton>
          ) : (
            <FormButton variant='ghost' size='sm' fullWidth={false} loading={busy} onClick={onActivate}>
              Activate
            </FormButton>
          )}
          <FormButton
            variant='ghost'
            size='icon'
            fullWidth={false}
            loading={busy}
            onClick={onDelete}
            title='Delete product'
          >
            <Trash2 className='text-destructive size-4' />
          </FormButton>
        </div>
      </TableCell>
    </TableRow>
  );
}
