CREATE SCHEMA IF NOT EXISTS procurement;

-- supplier_id references supplier.suppliers by UUID only.
CREATE TABLE procurement.purchase_orders (
    id           UUID PRIMARY KEY,
    supplier_id  UUID NOT NULL,
    status       VARCHAR(20) NOT NULL,
    subtotal     NUMERIC(14, 2),
    placed_at    TIMESTAMPTZ,
    received_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL,
    created_by   UUID,
    updated_at   TIMESTAMPTZ NOT NULL,
    updated_by   UUID
);

-- product_id references catalogue.products by UUID only. unit_cost/line_total are captured at the
-- moment the line was added, not recomputed later.
CREATE TABLE procurement.purchase_order_lines (
    id                UUID PRIMARY KEY,
    purchase_order_id UUID NOT NULL REFERENCES procurement.purchase_orders (id),
    product_id        UUID NOT NULL,
    quantity          NUMERIC(14, 3) NOT NULL,
    unit_cost         NUMERIC(14, 2) NOT NULL,
    line_total        NUMERIC(14, 2) NOT NULL
);

CREATE INDEX idx_purchase_order_lines_purchase_order_id ON procurement.purchase_order_lines (purchase_order_id);
CREATE INDEX idx_purchase_orders_supplier_id ON procurement.purchase_orders (supplier_id);
