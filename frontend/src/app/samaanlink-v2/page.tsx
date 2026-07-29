'use client';

/**
 * Standalone verification page for the new com.samaanlink backend (Task 7 thin slice).
 *
 * Deliberately outside the (app) route group: that layout gates on the legacy session-key
 * `useSession()` and would redirect to /sign-in. This page manages its own JWT state and proves
 * the new modules (Identity, Catalogue, Pricing, Restaurant, Orders) work end-to-end from a real
 * browser before any UI screens get rebuilt against them for real.
 */

import { useState } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription
} from '@/components/ui/card';
import { FormButton } from '@/components/ui/form-button';
import { formatMoney } from '@/lib/utils';
import {
  login,
  createCategory,
  createProduct,
  activateProduct,
  setPurchasePrice,
  setStandardSellingPrice,
  registerRestaurant,
  activateRestaurant,
  addDeliveryAddress,
  createOrder,
  addOrderLine,
  placeOrder,
  SamaanLinkApiError,
  type OrderSummary
} from '@/lib/samaanlink-api';

type StepStatus = 'pending' | 'running' | 'ok' | 'error';

interface Step {
  label: string;
  status: StepStatus;
  detail?: string;
}

const ADMIN_EMAIL = 'admin@samaanlink.dev';
const ADMIN_PASSWORD = 'ChangeMe123!';

export default function SamaanLinkV2Page() {
  const [steps, setSteps] = useState<Step[]>([]);
  const [running, setRunning] = useState(false);
  const [order, setOrder] = useState<OrderSummary | null>(null);

  function pushStep(label: string): number {
    let index = -1;
    setSteps((prev) => {
      index = prev.length;
      return [...prev, { label, status: 'running' }];
    });
    return index;
  }

  function resolveStep(index: number, status: StepStatus, detail?: string) {
    setSteps((prev) =>
      prev.map((s, i) => (i === index ? { ...s, status, detail } : s))
    );
  }

  async function step<T>(label: string, fn: () => Promise<T>): Promise<T> {
    const index = pushStep(label);
    try {
      const result = await fn();
      resolveStep(index, 'ok', typeof result === 'string' ? result : undefined);
      return result;
    } catch (err) {
      const message =
        err instanceof SamaanLinkApiError ? err.message : 'Unexpected error';
      resolveStep(index, 'error', message);
      throw err;
    }
  }

  async function runFlow() {
    setRunning(true);
    setSteps([]);
    setOrder(null);
    const suffix = Date.now();

    try {
      const admin = await step('Sign in as SUPER_ADMIN', () =>
        login(ADMIN_EMAIL, ADMIN_PASSWORD)
      );

      const category = await step('Create catalogue category', () =>
        createCategory(admin.accessToken, `Verification Produce ${suffix}`)
      );

      const product = await step('Create product', () =>
        createProduct(admin.accessToken, {
          name: `Verification Banana ${suffix}`,
          categoryId: category.id,
          sku: `SKU-V2-${suffix}`,
          purchaseUnitCode: 'KG',
          sellingUnitCode: 'KG',
          packageSize: 1,
          unitsPerPackage: 1
        })
      );

      await step('Activate product', () =>
        activateProduct(admin.accessToken, product.id)
      );

      await step('Set purchase price ($20.00)', () =>
        setPurchasePrice(admin.accessToken, product.id, 20)
      );

      await step('Set standard selling price ($25.00)', () =>
        setStandardSellingPrice(admin.accessToken, product.id, 25)
      );

      const ownerEmail = `owner-v2-${suffix}@test.samaanlink.dev`;
      const ownerPassword = 'SuperSecret1';

      const registered = await step('Register restaurant', () =>
        registerRestaurant(admin.accessToken, {
          restaurantName: `Verification Diner ${suffix}`,
          creditLimit: 500,
          paymentTermDays: 14,
          primaryBranchName: 'Main Branch',
          primaryBranchCity: 'Mogadishu',
          ownerEmail,
          ownerPassword,
          ownerFirstName: 'Verification',
          ownerLastName: 'Owner'
        })
      );

      await step('Activate restaurant', () =>
        activateRestaurant(admin.accessToken, registered.restaurant.id)
      );

      const address = await step('Add delivery address', () =>
        addDeliveryAddress(admin.accessToken, registered.primaryBranchId, {
          label: 'HQ',
          addressLine: 'Main St 1',
          city: 'Mogadishu',
          defaultAddress: true
        })
      );

      const owner = await step('Sign in as restaurant owner', () =>
        login(ownerEmail, ownerPassword)
      );

      const draft = await step('Create draft order', () =>
        createOrder(owner.accessToken, registered.restaurant.id, address.id)
      );

      await step('Add order line (10 units)', () =>
        addOrderLine(owner.accessToken, draft.id, product.id, 10)
      );

      const placed = await step('Place order', () =>
        placeOrder(owner.accessToken, draft.id)
      );

      setOrder(placed);
    } catch {
      // Individual step already recorded the failure; stop the flow.
    } finally {
      setRunning(false);
    }
  }

  return (
    <div className='mx-auto max-w-2xl px-4 py-10'>
      <Card>
        <CardHeader>
          <CardTitle className='text-2xl'>
            SamaanLink backend verification
          </CardTitle>
          <CardDescription>
            Exercises Identity, Catalogue, Pricing, Restaurant and Orders
            end-to-end against the new com.samaanlink API - not the final
            ordering UI, just proof the new stack works before the real frontend
            rewrite (Task 7).
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-6'>
          <FormButton
            onClick={runFlow}
            loading={running}
            loadingText='Running flow…'
          >
            Run end-to-end flow
          </FormButton>

          {steps.length > 0 && (
            <ol className='space-y-2 text-sm'>
              {steps.map((s, i) => (
                <li key={i} className='flex items-start gap-2'>
                  <span
                    className={
                      s.status === 'ok'
                        ? 'text-green-600'
                        : s.status === 'error'
                          ? 'text-destructive'
                          : s.status === 'running'
                            ? 'text-muted-foreground animate-pulse'
                            : 'text-muted-foreground'
                    }
                  >
                    {s.status === 'ok'
                      ? '✓'
                      : s.status === 'error'
                        ? '✗'
                        : s.status === 'running'
                          ? '…'
                          : '·'}
                  </span>
                  <span>
                    {s.label}
                    {s.detail && (
                      <span className='text-muted-foreground'>
                        {' '}
                        — {s.detail}
                      </span>
                    )}
                  </span>
                </li>
              ))}
            </ol>
          )}

          {order && (
            <Card className='bg-muted/40'>
              <CardHeader>
                <CardTitle className='text-base'>
                  Placed order {order.id}
                </CardTitle>
                <CardDescription>Status: {order.status}</CardDescription>
              </CardHeader>
              <CardContent className='space-y-1 text-sm'>
                <p>Subtotal: {formatMoney(order.subtotal)}</p>
                <p>Delivery fee: {formatMoney(order.deliveryFee)}</p>
                <p className='font-semibold'>
                  Order total: {formatMoney(order.orderTotal)}
                </p>
                <p className='text-muted-foreground pt-2'>
                  {order.lines.length} line(s)
                </p>
              </CardContent>
            </Card>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
