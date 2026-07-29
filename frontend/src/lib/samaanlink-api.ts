/**
 * com.samaanlink API client - the new JWT-secured modular backend, separate from the legacy
 * session-key `foodyFetch` client in `lib/api.ts`. Routed through `/api/samaanlink/*` (see
 * next.config.ts), which forwards to the backend's `/api/v1/*`.
 *
 * This client only covers what the Task 7 end-to-end verification slice
 * (`/samaanlink-v2`) exercises: auth, catalogue browsing, restaurant onboarding, pricing, and
 * order placement. It is not the final production API client for the rebuilt frontend.
 */

const API_BASE = '/api/samaanlink';

export class SamaanLinkApiError extends Error {}

async function request<T>(
  path: string,
  token: string | null,
  init: RequestInit = {}
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(init.headers as Record<string, string>)
  };
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers,
    cache: 'no-store'
  });

  const text = await res.text();
  let body: unknown = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = text;
    }
  }

  if (!res.ok) {
    const message =
      body && typeof body === 'object' && body !== null && 'message' in body
        ? String(
            (body as { message?: unknown }).message ??
              `Request failed (${res.status})`
          )
        : typeof body === 'string' && body
          ? body
          : `Request failed (${res.status})`;
    throw new SamaanLinkApiError(message);
  }

  return body as T;
}

// ---------------------------------------------------------------------------
// Auth
// ---------------------------------------------------------------------------

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  userId: string;
  roleName: string;
}

export function login(email: string, password: string) {
  return request<AuthResponse>('/auth/login', null, {
    method: 'POST',
    body: JSON.stringify({ email, password })
  });
}

// ---------------------------------------------------------------------------
// Catalogue
// ---------------------------------------------------------------------------

export interface CategorySummary {
  id: string;
  name: string;
  parentCategoryId: string | null;
  status: string;
}

export interface ProductSummary {
  id: string;
  name: string;
  sku: string;
  barcode: string | null;
  categoryId: string;
  categoryName: string;
  purchaseUnitCode: string;
  sellingUnitCode: string;
  packageSize: string;
  unitsPerPackage: string;
  weightKg: string | null;
  status: string;
}

export function createCategory(
  token: string,
  name: string,
  parentCategoryId?: string
) {
  return request<CategorySummary>('/categories', token, {
    method: 'POST',
    body: JSON.stringify({ name, parentCategoryId })
  });
}

export function listCategories(token: string) {
  return request<CategorySummary[]>('/categories', token);
}

export function createProduct(
  token: string,
  input: {
    name: string;
    description?: string;
    categoryId: string;
    sku: string;
    barcode?: string;
    purchaseUnitCode: string;
    sellingUnitCode: string;
    packageSize: number;
    unitsPerPackage: number;
    weightKg?: number;
  }
) {
  return request<ProductSummary>('/products', token, {
    method: 'POST',
    body: JSON.stringify(input)
  });
}

export function activateProduct(token: string, productId: string) {
  return request<void>(`/products/${productId}/activate`, token, {
    method: 'PUT'
  });
}

export function listProductsByCategory(token: string, categoryId: string) {
  return request<ProductSummary[]>(
    `/products?categoryId=${encodeURIComponent(categoryId)}`,
    token
  );
}

// ---------------------------------------------------------------------------
// Pricing
// ---------------------------------------------------------------------------

export function setPurchasePrice(
  token: string,
  productId: string,
  price: number
) {
  return request<void>(`/pricing/products/${productId}/purchase-price`, token, {
    method: 'PUT',
    body: JSON.stringify({ price })
  });
}

export function setStandardSellingPrice(
  token: string,
  productId: string,
  price: number
) {
  return request<void>(`/pricing/products/${productId}/selling-price`, token, {
    method: 'PUT',
    body: JSON.stringify({ price })
  });
}

// ---------------------------------------------------------------------------
// Restaurant
// ---------------------------------------------------------------------------

export interface RestaurantSummary {
  id: string;
  name: string;
  creditLimit: string;
  paymentTermDays: number;
  status: string;
}

export interface RegisteredRestaurant {
  restaurant: RestaurantSummary;
  ownerUserId: string;
  primaryBranchId: string;
}

export function registerRestaurant(
  token: string,
  input: {
    restaurantName: string;
    creditLimit: number;
    paymentTermDays: number;
    primaryBranchName: string;
    primaryBranchCity: string;
    ownerEmail: string;
    ownerPassword: string;
    ownerFirstName: string;
    ownerLastName: string;
    ownerPhone?: string;
  }
) {
  return request<RegisteredRestaurant>('/restaurants', token, {
    method: 'POST',
    body: JSON.stringify(input)
  });
}

export function activateRestaurant(token: string, restaurantId: string) {
  return request<void>(`/restaurants/${restaurantId}/activate`, token, {
    method: 'PUT'
  });
}

export interface DeliveryAddressSummary {
  id: string;
  branchId: string;
  label: string;
  addressLine: string;
  city: string;
  defaultAddress: boolean;
}

export function addDeliveryAddress(
  token: string,
  branchId: string,
  input: {
    label: string;
    addressLine: string;
    city: string;
    defaultAddress: boolean;
  }
) {
  return request<DeliveryAddressSummary>(
    `/restaurants/branches/${branchId}/addresses`,
    token,
    { method: 'POST', body: JSON.stringify(input) }
  );
}

// ---------------------------------------------------------------------------
// Orders
// ---------------------------------------------------------------------------

export interface OrderLineSummary {
  id: string;
  productId: string;
  quantity: string;
  priceQuoteId: string;
  lineTotal: string;
}

export interface OrderSummary {
  id: string;
  restaurantId: string;
  deliveryAddressId: string;
  status: string;
  subtotal: string | null;
  deliveryFee: string | null;
  orderTotal: string | null;
  createdAt: string;
  placedAt: string | null;
  lines: OrderLineSummary[];
}

export function createOrder(
  token: string,
  restaurantId: string,
  deliveryAddressId: string
) {
  return request<OrderSummary>('/orders', token, {
    method: 'POST',
    body: JSON.stringify({ restaurantId, deliveryAddressId })
  });
}

export function addOrderLine(
  token: string,
  orderId: string,
  productId: string,
  quantity: number
) {
  return request<OrderLineSummary>(`/orders/${orderId}/lines`, token, {
    method: 'POST',
    body: JSON.stringify({ productId, quantity })
  });
}

export function placeOrder(token: string, orderId: string) {
  return request<OrderSummary>(`/orders/${orderId}/place`, token, {
    method: 'POST'
  });
}
