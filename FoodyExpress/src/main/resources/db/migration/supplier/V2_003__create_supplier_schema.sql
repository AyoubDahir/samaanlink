CREATE SCHEMA IF NOT EXISTS supplier;

CREATE TABLE supplier.suppliers (
    id               UUID PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    lead_time_days   INTEGER NOT NULL,
    payment_term_days INTEGER NOT NULL,
    status           VARCHAR(32) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    created_by       UUID,
    updated_by       UUID
);

CREATE TABLE supplier.supplier_contacts (
    id           UUID PRIMARY KEY,
    supplier_id  UUID NOT NULL REFERENCES supplier.suppliers(id),
    name         VARCHAR(150) NOT NULL,
    phone        VARCHAR(32),
    email        VARCHAR(255),
    role_title   VARCHAR(100)
);

-- product_id references catalogue.products(id) by UUID only, per the module-boundary rule that
-- no module holds a foreign key into another module's schema.
CREATE TABLE supplier.supplier_products (
    id             UUID PRIMARY KEY,
    supplier_id    UUID NOT NULL REFERENCES supplier.suppliers(id),
    product_id     UUID NOT NULL,
    supplier_sku   VARCHAR(64),
    UNIQUE (supplier_id, product_id)
);
