CREATE SCHEMA IF NOT EXISTS orders;

-- restaurant_id/delivery_address_id reference restaurant.restaurants/delivery_addresses by UUID only.
CREATE TABLE orders.orders (
    id                  UUID PRIMARY KEY,
    restaurant_id       UUID NOT NULL,
    delivery_address_id UUID NOT NULL,
    status              VARCHAR(20) NOT NULL,
    subtotal            NUMERIC(14, 2),
    delivery_fee        NUMERIC(14, 2),
    order_total         NUMERIC(14, 2),
    placed_at           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL,
    created_by          UUID,
    updated_at          TIMESTAMPTZ NOT NULL,
    updated_by          UUID
);

-- product_id/price_quote_id reference catalogue.products/pricing.price_snapshots by UUID only.
-- line_total is captured from the PriceQuote at the moment the line was added, not recomputed later.
CREATE TABLE orders.order_lines (
    id             UUID PRIMARY KEY,
    order_id       UUID NOT NULL REFERENCES orders.orders (id),
    product_id     UUID NOT NULL,
    quantity       NUMERIC(14, 3) NOT NULL,
    price_quote_id UUID NOT NULL,
    line_total     NUMERIC(14, 2) NOT NULL
);

CREATE INDEX idx_order_lines_order_id ON orders.order_lines (order_id);
CREATE INDEX idx_orders_restaurant_id ON orders.orders (restaurant_id);
