'use client';

import { useState } from 'react';
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
import { login, ApiError } from '@/lib/api';
import { setSession, homeRouteFor } from '@/lib/session';

export default function SignInPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await login(email, password);
      setSession({
        accessToken: res.accessToken,
        refreshToken: res.refreshToken,
        userId: res.userId,
        role: res.roleName
      });
      toast.success('Signed in');
      router.replace(homeRouteFor(res.roleName));
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Sign in failed');
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
            Sign in to manage the catalogue or place a restaurant order.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className='space-y-4'>
            <FormInput
              id='email'
              label='Email'
              type='email'
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder='you@example.com'
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

          <p className='text-muted-foreground mt-4 text-center text-xs'>
            Accounts are created by a platform admin (or automatically for a
            registered restaurant&apos;s owner) — there is no self-service
            sign-up.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
