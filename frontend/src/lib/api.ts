/**
 * FoodyExpress API client.
 *
 * Auth model: POST /app/login returns a short session `key` string (no JWT).
 * Every protected call passes that key as a `key` query param. Requests are
 * routed through the same-origin `/api/foody/*` Next.js rewrite (see
 * next.config.ts) so the browser never needs CORS against the backend.
 *
 * Known backend gaps (by design, not bugs): Item<->Restaurant is a hidden
 * (@JsonIgnore both sides) many-to-many, so there is no "menu for this
 * restaurant" endpoint — items are browsed by Category instead. Restaurants
 * are a separate directory (address/contact), not a menu owner in the UI.
 */

const API_BASE = '/api/foody';

export class FoodyApiError extends Error {}

function qs(
  params: Record<string, string | number | undefined | null>
): string {
  const usp = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null && v !== '') usp.set(k, String(v));
  }
  const s = usp.toString();
  return s ? `?${s}` : '';
}

async function foodyFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init.headers
    },
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
    throw new FoodyApiError(message);
  }

  return body as T;
}

// ---------------------------------------------------------------------------
// Types (mirror FoodyExpress JPA entities / DTOs)
// ---------------------------------------------------------------------------

export type Role = 'customer' | 'admin';

export interface LoginResponse {
  message: string;
  key: string;
  role: Role;
  customerId: number | null;
}

export interface Address {
  addressId?: number;
  buildingName?: string;
  streetNo?: string;
  area?: string;
  city?: string;
  state?: string;
  country?: string;
  pincode?: string;
}

export interface Category {
  categoryId: number;
  categoryName: string;
}

export interface Item {
  itemId: number;
  itemName: string;
  category: Category;
  quantity: number;
  cost: number;
}

export interface Restaurant {
  restaurantId: number;
  restaurantName: string;
  address: Address;
  managerName?: string;
  contactNunber?: string;
}

export interface FoodCart {
  cartId: number;
  itemList: Item[];
}

export interface Customer {
  customerId: number;
  firstName: string;
  lastName: string;
  age?: number | null;
  gender?: string | null;
  mobileNumber?: string;
  email: string;
  cart?: FoodCart;
}

export interface OrderDetails {
  orderId: number;
  orderDate: string;
  foodCart: FoodCart;
  orderStatus: string;
}

export interface Bill {
  billId: number;
  billDate: string;
  totalCost: number;
  totalItem: number;
  order: OrderDetails;
}

export interface OrderHistory {
  orderHistoryId: number;
  customerId: number;
  bill: Bill;
}

// ---------------------------------------------------------------------------
// Auth
// ---------------------------------------------------------------------------

export function login(email: string, password: string, role: Role) {
  return foodyFetch<LoginResponse>('/app/login', {
    method: 'POST',
    body: JSON.stringify({ email, password, role })
  });
}

export function logout(role: Role, key: string) {
  return foodyFetch<string>(`/app/logout${qs({ role, key })}`, {
    method: 'POST'
  });
}

// ---------------------------------------------------------------------------
// Customers
// ---------------------------------------------------------------------------

export function registerCustomer(customer: {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  mobileNumber?: string;
  age?: number;
  gender?: string;
}) {
  return foodyFetch<Customer>('/customers/add', {
    method: 'POST',
    body: JSON.stringify(customer)
  });
}

export function getCustomer(key: string, customerId: number) {
  return foodyFetch<Customer>(`/customers/all/${customerId}${qs({ key })}`);
}

// ---------------------------------------------------------------------------
// Categories
// ---------------------------------------------------------------------------

export function getAllCategories(key: string) {
  return foodyFetch<Category[]>(`/category/viewall${qs({ key })}`);
}

export function addCategory(key: string, categoryName: string) {
  return foodyFetch<Category>(`/category/add${qs({ key, categoryName })}`, {
    method: 'POST'
  });
}

export function updateCategory(key: string, category: Category) {
  return foodyFetch<Category>(`/category/update${qs({ key })}`, {
    method: 'PUT',
    body: JSON.stringify(category)
  });
}

export function removeCategory(key: string, categoryName: string) {
  return foodyFetch<Category>(`/category/remove${qs({ key, categoryName })}`, {
    method: 'DELETE'
  });
}

// ---------------------------------------------------------------------------
// Items (menu)
// ---------------------------------------------------------------------------

export function getAllItems(key: string) {
  return foodyFetch<Item[]>(`/items/all${qs({ key })}`);
}

export function getItemsByCategoryName(key: string, categoryName: string) {
  return foodyFetch<Item[]>(
    `/items/get/${encodeURIComponent(categoryName)}${qs({ key })}`
  );
}

export function addItem(
  key: string,
  item: {
    itemName: string;
    quantity: number;
    cost: number;
    categoryName: string;
  }
) {
  return foodyFetch<Item>(`/items/add${qs({ key })}`, {
    method: 'POST',
    body: JSON.stringify({
      itemName: item.itemName,
      quantity: item.quantity,
      cost: item.cost,
      category: { categoryName: item.categoryName }
    })
  });
}

export function updateItem(
  key: string,
  itemDTO: {
    itemId: number;
    catergoryId?: number;
    itemName?: string;
    quantity?: number;
    cost?: number;
  }
) {
  return foodyFetch<Item>(`/items/update${qs({ key })}`, {
    method: 'PUT',
    body: JSON.stringify(itemDTO)
  });
}

export function removeItem(key: string, itemId: number) {
  return foodyFetch<Item>(`/items/delete/${itemId}${qs({ key })}`, {
    method: 'DELETE'
  });
}

// ---------------------------------------------------------------------------
// Restaurants
// ---------------------------------------------------------------------------

export function getAllRestaurants(key: string) {
  return foodyFetch<Restaurant[]>(`/restaurants/view${qs({ key })}`);
}

export function getRestaurant(key: string, id: number) {
  return foodyFetch<Restaurant>(`/restaurants/view/${id}${qs({ key })}`);
}

export function findRestaurantsByCity(key: string, city: string) {
  return foodyFetch<Restaurant[]>(
    `/restaurants/findNearByRestaurantByCity/${encodeURIComponent(city)}${qs({ key })}`
  );
}

export function findRestaurantsByItemName(key: string, itemName: string) {
  return foodyFetch<Restaurant[]>(
    `/restaurants/findNearByRestaurantByItemName/${encodeURIComponent(itemName)}${qs({ key })}`
  );
}

export function addRestaurant(
  key: string,
  restaurant: {
    restaurantName: string;
    managerName?: string;
    contactNunber?: string;
    address: Address;
  }
) {
  return foodyFetch<Restaurant>(`/restaurants/add${qs({ key })}`, {
    method: 'POST',
    body: JSON.stringify({ ...restaurant, itemList: [] })
  });
}

export function updateRestaurant(key: string, restaurant: Restaurant) {
  return foodyFetch<Restaurant>(`/restaurants/update${qs({ key })}`, {
    method: 'PUT',
    body: JSON.stringify(restaurant)
  });
}

export function removeRestaurant(key: string, restaurantId: number) {
  return foodyFetch<Restaurant>(
    `/restaurants/delete${qs({ key, restaurantId })}`,
    { method: 'DELETE' }
  );
}

// ---------------------------------------------------------------------------
// Cart
// ---------------------------------------------------------------------------

export function addItemToCart(key: string, customerId: number, itemId: number) {
  return foodyFetch<FoodCart>(
    `/foodcart/addtocart/${customerId}${qs({ key, itemId })}`,
    {
      method: 'POST'
    }
  );
}

export function increaseItemQuantity(
  key: string,
  cartId: number,
  itemId: number,
  quantity: number
) {
  return foodyFetch<FoodCart>(
    `/foodcart/increaseQuantity${qs({ key, cartId, itemId, quantity })}`,
    {
      method: 'PUT'
    }
  );
}

export function decreaseItemQuantity(
  key: string,
  cartId: number,
  itemId: number,
  quantity: number
) {
  return foodyFetch<FoodCart>(
    `/foodcart/decreaseQuantity${qs({ key, cartId, itemId, quantity })}`,
    {
      method: 'PUT'
    }
  );
}

export function removeItemFromCart(
  key: string,
  cartId: number,
  itemId: number
) {
  return foodyFetch<FoodCart>(`/foodcart/item${qs({ key, cartId, itemId })}`, {
    method: 'DELETE'
  });
}

export function clearCart(key: string, cartId: number) {
  return foodyFetch<FoodCart>(`/foodcart/delete${qs({ key, cartId })}`, {
    method: 'DELETE'
  });
}

// ---------------------------------------------------------------------------
// Orders
// ---------------------------------------------------------------------------

export function placeOrder(key: string, customerId: number) {
  return foodyFetch<OrderDetails>(`/order/add/${customerId}${qs({ key })}`, {
    method: 'POST'
  });
}

export function getOrder(key: string, orderId: number) {
  return foodyFetch<OrderDetails>(`/order/view/${orderId}${qs({ key })}`);
}

export function removeOrder(key: string, orderId: number) {
  return foodyFetch<OrderDetails>(`/order/remove/${orderId}${qs({ key })}`, {
    method: 'DELETE'
  });
}

// ---------------------------------------------------------------------------
// Bills
// ---------------------------------------------------------------------------

export function generateBill(key: string, customerId: number, orderId: number) {
  return foodyFetch<Bill>(`/bill/add${qs({ key, customerId, orderId })}`, {
    method: 'POST'
  });
}

export function getBill(key: string, billId: number) {
  return foodyFetch<Bill>(`/bill/view/${billId}${qs({ key })}`);
}

// ---------------------------------------------------------------------------
// Order history
// ---------------------------------------------------------------------------

export function getOrderHistoryByCustomer(key: string, customerId: number) {
  return foodyFetch<OrderHistory[]>(
    `/order-history/customer-id${qs({ key, customerId })}`
  );
}

export function getAllOrderHistory(key: string) {
  return foodyFetch<OrderHistory[]>(`/order-history/all${qs({ key })}`);
}
