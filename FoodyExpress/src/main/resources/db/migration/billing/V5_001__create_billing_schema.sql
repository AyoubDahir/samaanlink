CREATE SCHEMA IF NOT EXISTS billing;

-- order_id/restaurant_id reference orders.orders/restaurant.restaurants by UUID only.
-- amount is captured from the order's frozen orderTotal at issue time, not recomputed later.
CREATE TABLE billing.bills (
    id            UUID PRIMARY KEY,
    order_id      UUID NOT NULL UNIQUE,
    restaurant_id UUID NOT NULL,
    amount        NUMERIC(14, 2) NOT NULL,
    status        VARCHAR(20) NOT NULL,
    paid_at       TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL,
    created_by    UUID,
    updated_at    TIMESTAMPTZ NOT NULL,
    updated_by    UUID
);

CREATE INDEX idx_bills_restaurant_id ON billing.bills (restaurant_id);
