CREATE SCHEMA IF NOT EXISTS catalogue;

CREATE TABLE catalogue.categories (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    parent_category_id  UUID REFERENCES catalogue.categories(id),
    status              VARCHAR(32) NOT NULL
);

CREATE TABLE catalogue.units_of_measure (
    id    UUID PRIMARY KEY,
    code  VARCHAR(16) NOT NULL UNIQUE,
    name  VARCHAR(64) NOT NULL
);

CREATE TABLE catalogue.products (
    id                 UUID PRIMARY KEY,
    name               VARCHAR(200) NOT NULL,
    description        VARCHAR(2000),
    category_id        UUID NOT NULL REFERENCES catalogue.categories(id),
    sku                VARCHAR(64) NOT NULL UNIQUE,
    barcode            VARCHAR(64),
    purchase_unit_id   UUID NOT NULL REFERENCES catalogue.units_of_measure(id),
    selling_unit_id    UUID NOT NULL REFERENCES catalogue.units_of_measure(id),
    package_size       NUMERIC(14, 3) NOT NULL,
    units_per_package  NUMERIC(14, 3) NOT NULL,
    weight_kg          NUMERIC(14, 3),
    status             VARCHAR(32) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    created_by         UUID,
    updated_by         UUID
);

CREATE TABLE catalogue.product_images (
    id          UUID PRIMARY KEY,
    product_id  UUID NOT NULL REFERENCES catalogue.products(id),
    url         VARCHAR(1000) NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0
);
