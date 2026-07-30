/**
 * com.samaanlink API client - the JWT-secured B2B procurement backend. Routed through
 * `/api/samaanlink/*` (see next.config.ts), which forwards to the backend's `/api/v1/*`.
 */

const API_BASE = '/api/samaanlink';

export class ApiError extends Error {}

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
    throw new ApiError(message);
  }

  return body as T;
}

// ---------------------------------------------------------------------------
// Auth
// ---------------------------------------------------------------------------

export type AppRole =
  | 'SUPER_ADMIN'
  | 'COMPANY_MANAGER'
  | 'FINANCE_OFFICER'
  | 'PROCUREMENT_OFFICER'
  | 'WAREHOUSE_OFFICER'
  | 'SALES_OFFICER'
  | 'DELIVERY_COORDINATOR'
  | 'DRIVER'
  | 'RESTAURANT_OWNER'
  | 'RESTAURANT_STAFF';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  userId: string;
  roleName: AppRole;
}

export function login(email: string, password: string) {
  return request<AuthResponse>('/auth/login', null, {
    method: 'POST',
    body: JSON.stringify({ email, password })
  });
}

export function logout(refreshToken: string) {
  return request<void>('/auth/logout', null, {
    method: 'POST',
    body: JSON.stringify({ refreshToken })
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

export function listProducts(token: string) {
  return request<ProductSummary[]>('/products', token);
}

export function listProductsByCategory(token: string, categoryId: string) {
  return request<ProductSummary[]>(
    `/products?categoryId=${encodeURIComponent(categoryId)}`,
    token
  );
}

export function activateProduct(token: string, productId: string) {
  return request<void>(`/products/${productId}/activate`, token, {
    method: 'PUT'
  });
}

export function discontinueProduct(token: string, productId: string) {
  return request<void>(`/products/${productId}/discontinue`, token, {
    method: 'PUT'
  });
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

export function listRestaurants(token: string) {
  return request<RestaurantSummary[]>('/restaurants', token);
}

/** Resolves the logged-in restaurant owner/staff user's own restaurant. */
export function myRestaurant(token: string) {
  return request<RestaurantSummary>('/restaurants/me', token);
}

export function activateRestaurant(token: string, restaurantId: string) {
  return request<void>(`/restaurants/${restaurantId}/activate`, token, {
    method: 'PUT'
  });
}

export function suspendRestaurant(token: string, restaurantId: string) {
  return request<void>(`/restaurants/${restaurantId}/suspend`, token, {
    method: 'PUT'
  });
}

export interface BranchSummary {
  id: string;
  restaurantId: string;
  name: string;
  city: string;
  primary: boolean;
}

export function listBranches(token: string, restaurantId: string) {
  return request<BranchSummary[]>(`/restaurants/${restaurantId}/branches`, token);
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

export function listDeliveryAddresses(token: string, branchId: string) {
  return request<DeliveryAddressSummary[]>(
    `/restaurants/branches/${branchId}/addresses`,
    token
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

export function removeOrderLine(token: string, orderId: string, lineId: string) {
  return request<void>(`/orders/${orderId}/lines/${lineId}`, token, {
    method: 'DELETE'
  });
}

export function placeOrder(token: string, orderId: string) {
  return request<OrderSummary>(`/orders/${orderId}/place`, token, {
    method: 'POST'
  });
}

export function cancelOrder(token: string, orderId: string) {
  return request<void>(`/orders/${orderId}/cancel`, token, {
    method: 'POST'
  });
}

export function findOrder(token: string, orderId: string) {
  return request<OrderSummary>(`/orders/${orderId}`, token);
}

export function listOrdersByRestaurant(token: string, restaurantId: string) {
  return request<OrderSummary[]>(
    `/orders?restaurantId=${encodeURIComponent(restaurantId)}`,
    token
  );
}

// ---------------------------------------------------------------------------
// Billing
// ---------------------------------------------------------------------------

export interface BillSummary {
  id: string;
  orderId: string;
  restaurantId: string;
  amount: string;
  status: string;
  issuedAt: string;
  paidAt: string | null;
}

export function generateBill(token: string, orderId: string) {
  return request<BillSummary>('/bills', token, {
    method: 'POST',
    body: JSON.stringify({ orderId })
  });
}

export function findBillByOrder(token: string, orderId: string) {
  return request<BillSummary>(`/bills/by-order/${orderId}`, token);
}

export function payBill(token: string, billId: string) {
  return request<BillSummary>(`/bills/${billId}/pay`, token, {
    method: 'POST'
  });
}

export function listBillsByRestaurant(token: string, restaurantId: string) {
  return request<BillSummary[]>(
    `/bills?restaurantId=${encodeURIComponent(restaurantId)}`,
    token
  );
}

export function listAllOrders(token: string) {
  return request<OrderSummary[]>('/orders', token);
}

export function markOrderDelivered(token: string, orderId: string) {
  return request<void>(`/orders/${orderId}/deliver`, token, {
    method: 'POST'
  });
}

// ---------------------------------------------------------------------------
// Supplier
// ---------------------------------------------------------------------------

export interface SupplierSummary {
  id: string;
  name: string;
  leadTimeDays: number;
  paymentTermDays: number;
  status: string;
}

export function registerSupplier(
  token: string,
  input: { name: string; leadTimeDays: number; paymentTermDays: number }
) {
  return request<SupplierSummary>('/suppliers', token, {
    method: 'POST',
    body: JSON.stringify(input)
  });
}

export function listSuppliers(token: string) {
  return request<SupplierSummary[]>('/suppliers', token);
}

export function activateSupplier(token: string, supplierId: string) {
  return request<void>(`/suppliers/${supplierId}/activate`, token, {
    method: 'PUT'
  });
}

export function suspendSupplier(token: string, supplierId: string) {
  return request<void>(`/suppliers/${supplierId}/suspend`, token, {
    method: 'PUT'
  });
}

export function linkSupplierProduct(
  token: string,
  supplierId: string,
  productId: string,
  supplierSku?: string
) {
  return request<void>(`/suppliers/${supplierId}/products`, token, {
    method: 'POST',
    body: JSON.stringify({ productId, supplierSku })
  });
}

export function listSupplierProductIds(token: string, supplierId: string) {
  return request<string[]>(`/suppliers/${supplierId}/products`, token);
}

// ---------------------------------------------------------------------------
// Procurement (platform buying from a supplier)
// ---------------------------------------------------------------------------

export interface PurchaseOrderLineSummary {
  id: string;
  productId: string;
  quantity: string;
  unitCost: string;
  lineTotal: string;
}

export interface PurchaseOrderSummary {
  id: string;
  supplierId: string;
  status: string;
  subtotal: string | null;
  createdAt: string;
  placedAt: string | null;
  receivedAt: string | null;
  lines: PurchaseOrderLineSummary[];
}

export function createPurchaseOrder(token: string, supplierId: string) {
  return request<PurchaseOrderSummary>('/purchase-orders', token, {
    method: 'POST',
    body: JSON.stringify({ supplierId })
  });
}

export function addPurchaseOrderLine(
  token: string,
  purchaseOrderId: string,
  productId: string,
  quantity: number,
  unitCost: number
) {
  return request<PurchaseOrderLineSummary>(`/purchase-orders/${purchaseOrderId}/lines`, token, {
    method: 'POST',
    body: JSON.stringify({ productId, quantity, unitCost })
  });
}

export function removePurchaseOrderLine(token: string, purchaseOrderId: string, lineId: string) {
  return request<void>(`/purchase-orders/${purchaseOrderId}/lines/${lineId}`, token, {
    method: 'DELETE'
  });
}

export function placePurchaseOrder(token: string, purchaseOrderId: string) {
  return request<PurchaseOrderSummary>(`/purchase-orders/${purchaseOrderId}/place`, token, {
    method: 'POST'
  });
}

export function receivePurchaseOrder(token: string, purchaseOrderId: string) {
  return request<PurchaseOrderSummary>(`/purchase-orders/${purchaseOrderId}/receive`, token, {
    method: 'POST'
  });
}

export function cancelPurchaseOrder(token: string, purchaseOrderId: string) {
  return request<void>(`/purchase-orders/${purchaseOrderId}/cancel`, token, {
    method: 'POST'
  });
}

export function findPurchaseOrder(token: string, purchaseOrderId: string) {
  return request<PurchaseOrderSummary>(`/purchase-orders/${purchaseOrderId}`, token);
}

export function listAllPurchaseOrders(token: string) {
  return request<PurchaseOrderSummary[]>('/purchase-orders', token);
}
