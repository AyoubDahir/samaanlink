CREATE SCHEMA IF NOT EXISTS pricing;

-- product_id/restaurant_id reference catalogue.products/restaurant.restaurants by UUID only.
CREATE TABLE pricing.purchase_prices (
    product_id  UUID PRIMARY KEY,
    price       NUMERIC(14, 2) NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE pricing.standard_selling_prices (
    product_id  UUID PRIMARY KEY,
    price       NUMERIC(14, 2) NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE pricing.restaurant_prices (
    id             UUID PRIMARY KEY,
    product_id     UUID NOT NULL,
    restaurant_id  UUID NOT NULL,
    price          NUMERIC(14, 2) NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    UNIQUE (product_id, restaurant_id)
);

CREATE TABLE pricing.product_discounts (
    product_id       UUID PRIMARY KEY,
    discount_percent NUMERIC(5, 2) NOT NULL
);

CREATE TABLE pricing.product_taxes (
    product_id  UUID PRIMARY KEY,
    tax_percent NUMERIC(5, 2) NOT NULL
);

-- Single active row each for MVP - one global service-fee rate and one global flat delivery fee.
CREATE TABLE pricing.service_fee_rules (
    id           UUID PRIMARY KEY,
    rate_percent NUMERIC(5, 2) NOT NULL
);

CREATE TABLE pricing.delivery_fee_rules (
    id       UUID PRIMARY KEY,
    flat_fee NUMERIC(14, 2) NOT NULL
);

-- Immutable once created - never updated, only ever inserted and read.
CREATE TABLE pricing.price_snapshots (
    id                  UUID PRIMARY KEY,
    product_id          UUID NOT NULL,
    restaurant_id       UUID NOT NULL,
    quantity            NUMERIC(14, 3) NOT NULL,
    unit_purchase_price NUMERIC(14, 2) NOT NULL,
    unit_selling_price  NUMERIC(14, 2) NOT NULL,
    line_subtotal       NUMERIC(14, 2) NOT NULL,
    discount_amount     NUMERIC(14, 2) NOT NULL,
    service_fee_amount  NUMERIC(14, 2) NOT NULL,
    tax_amount          NUMERIC(14, 2) NOT NULL,
    line_total          NUMERIC(14, 2) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL
);
