'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent
} from '@/components/ui/card';
import { FormInput } from '@/components/ui/form-input';
import { FormButton } from '@/components/ui/form-button';
import { login, FoodyApiError, type Role } from '@/lib/api';
import { setSession } from '@/lib/session';

export default function SignInPage() {
  const router = useRouter();
  const [role, setRole] = useState<Role>('customer');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await login(email, password, role);
      setSession({
        key: res.key,
        role: res.role,
        email,
        customerId: res.customerId ?? undefined
      });
      toast.success(res.message);
      router.replace(res.role === 'admin' ? '/admin/categories' : '/menu');
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Sign in failed'
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className='flex min-h-dvh items-center justify-center px-4'>
      <Card className='w-full max-w-sm'>
        <CardHeader>
          <CardTitle className='text-primary text-2xl'>SamaanLink</CardTitle>
          <CardDescription>
            Sign in to order food or manage the catalog.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className='space-y-4'>
            <div className='bg-muted grid grid-cols-2 gap-1 rounded-md p-1'>
              {(['customer', 'admin'] as const).map((r) => (
                <button
                  type='button'
                  key={r}
                  onClick={() => setRole(r)}
                  className={`rounded-sm py-1.5 text-sm font-medium capitalize transition-colors ${
                    role === r
                      ? 'bg-background text-foreground shadow-sm'
                      : 'text-muted-foreground'
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>

            <FormInput
              id='email'
              label='Email'
              type='email'
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder={
                role === 'admin' ? 'admin@gmail.com' : 'you@example.com'
              }
            />
            <FormInput
              id='password'
              label='Password'
              type='password'
              required
              showPasswordToggle
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />

            <FormButton
              type='submit'
              loading={loading}
              loadingText='Signing in…'
            >
              Sign in
            </FormButton>
          </form>

          {role === 'customer' && (
            <p className='text-muted-foreground mt-4 text-center text-sm'>
              No account?{' '}
              <Link href='/sign-up' className='text-primary font-medium'>
                Sign up
              </Link>
            </p>
          )}
          {role === 'admin' && (
            <p className='text-muted-foreground mt-4 text-center text-xs'>
              The admin account is bootstrapped via the backend&apos;s{' '}
              <code>POST /admin/new</code> endpoint.
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
