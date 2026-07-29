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
import { registerCustomer, login, FoodyApiError } from '@/lib/api';
import { setSession } from '@/lib/session';

export default function SignUpPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    mobileNumber: ''
  });
  const [loading, setLoading] = useState(false);
  const [mobileError, setMobileError] = useState<string | undefined>();

  function update(key: keyof typeof form) {
    return (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm((f) => ({ ...f, [key]: e.target.value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (form.mobileNumber.length !== 10) {
      setMobileError('Must be exactly 10 digits');
      return;
    }
    setMobileError(undefined);
    setLoading(true);
    try {
      await registerCustomer(form);
      const res = await login(form.email, form.password, 'customer');
      setSession({
        key: res.key,
        role: 'customer',
        email: form.email,
        customerId: res.customerId ?? undefined,
        firstName: form.firstName
      });
      toast.success('Account created!');
      router.replace('/menu');
    } catch (err) {
      toast.error(
        err instanceof FoodyApiError ? err.message : 'Sign up failed'
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className='flex min-h-dvh items-center justify-center px-4 py-8'>
      <Card className='w-full max-w-sm'>
        <CardHeader>
          <CardTitle className='text-primary text-2xl'>
            Create account
          </CardTitle>
          <CardDescription>
            Sign up to start ordering on SamaanLink.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className='space-y-4'>
            <div className='grid grid-cols-2 gap-3'>
              <FormInput
                id='firstName'
                label='First name'
                required
                value={form.firstName}
                onChange={update('firstName')}
              />
              <FormInput
                id='lastName'
                label='Last name'
                required
                value={form.lastName}
                onChange={update('lastName')}
              />
            </div>
            <FormInput
              id='email'
              label='Email'
              type='email'
              required
              value={form.email}
              onChange={update('email')}
            />
            <FormInput
              id='mobileNumber'
              label='Mobile number'
              required
              inputMode='numeric'
              maxLength={10}
              error={mobileError}
              description={mobileError ? undefined : '10 digits'}
              value={form.mobileNumber}
              onChange={(e) => {
                setMobileError(undefined);
                setForm((f) => ({
                  ...f,
                  mobileNumber: e.target.value.replace(/\D/g, '').slice(0, 10)
                }));
              }}
            />
            <FormInput
              id='password'
              label='Password'
              type='password'
              required
              showPasswordToggle
              value={form.password}
              onChange={update('password')}
            />

            <FormButton
              type='submit'
              loading={loading}
              loadingText='Creating account…'
            >
              Sign up
            </FormButton>
          </form>

          <p className='text-muted-foreground mt-4 text-center text-sm'>
            Already have an account?{' '}
            <Link href='/sign-in' className='text-primary font-medium'>
              Sign in
            </Link>
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
