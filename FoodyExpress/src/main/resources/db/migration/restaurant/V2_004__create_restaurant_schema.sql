CREATE SCHEMA IF NOT EXISTS restaurant;

CREATE TABLE restaurant.restaurants (
    id                 UUID PRIMARY KEY,
    name               VARCHAR(200) NOT NULL,
    credit_limit       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    payment_term_days  INTEGER NOT NULL DEFAULT 0,
    status             VARCHAR(32) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    created_by         UUID,
    updated_by         UUID
);

CREATE TABLE restaurant.restaurant_branches (
    id             UUID PRIMARY KEY,
    restaurant_id  UUID NOT NULL REFERENCES restaurant.restaurants(id),
    name           VARCHAR(150) NOT NULL,
    city           VARCHAR(100),
    is_primary     BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE restaurant.delivery_addresses (
    id           UUID PRIMARY KEY,
    branch_id    UUID NOT NULL REFERENCES restaurant.restaurant_branches(id),
    label        VARCHAR(100),
    address_line VARCHAR(500) NOT NULL,
    city         VARCHAR(100),
    is_default   BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE restaurant.restaurant_contacts (
    id             UUID PRIMARY KEY,
    restaurant_id  UUID NOT NULL REFERENCES restaurant.restaurants(id),
    name           VARCHAR(150) NOT NULL,
    phone          VARCHAR(32),
    email          VARCHAR(255),
    role_title     VARCHAR(100)
);

-- user_id references identity.users(id) by UUID only, per the module-boundary rule.
CREATE TABLE restaurant.restaurant_users (
    id             UUID PRIMARY KEY,
    restaurant_id  UUID NOT NULL REFERENCES restaurant.restaurants(id),
    user_id        UUID NOT NULL UNIQUE,
    added_at       TIMESTAMPTZ NOT NULL
);
