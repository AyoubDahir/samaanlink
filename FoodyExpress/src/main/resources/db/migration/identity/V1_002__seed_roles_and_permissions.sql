INSERT INTO identity.roles (id, name, description) VALUES
    (gen_random_uuid(), 'SUPER_ADMIN', 'Full system access'),
    (gen_random_uuid(), 'COMPANY_MANAGER', 'Manages restaurants, suppliers, catalogue, pricing, procurement and orders'),
    (gen_random_uuid(), 'FINANCE_OFFICER', 'Manages accounting and payments'),
    (gen_random_uuid(), 'PROCUREMENT_OFFICER', 'Manages procurement and supplier relationships'),
    (gen_random_uuid(), 'WAREHOUSE_OFFICER', 'Manages inventory and goods receipt'),
    (gen_random_uuid(), 'SALES_OFFICER', 'Manages restaurant orders on behalf of customers'),
    (gen_random_uuid(), 'DELIVERY_COORDINATOR', 'Assigns and tracks deliveries'),
    (gen_random_uuid(), 'DRIVER', 'Executes assigned deliveries'),
    (gen_random_uuid(), 'RESTAURANT_OWNER', 'Owns a restaurant account'),
    (gen_random_uuid(), 'RESTAURANT_STAFF', 'Restaurant staff acting on behalf of a restaurant owner');

-- Identity module's own permission catalogue. Each future module adds its own permission rows
-- (and the corresponding role_permissions grants) in its own Flyway migration as it is built,
-- rather than having them all speculatively predefined here.
INSERT INTO identity.permissions (id, code, description) VALUES
    (gen_random_uuid(), 'identity:users:read', 'View users'),
    (gen_random_uuid(), 'identity:users:manage', 'Create, activate and suspend users');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r, identity.permissions p
WHERE r.name = 'SUPER_ADMIN' AND p.code IN ('identity:users:read', 'identity:users:manage');

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM identity.roles r, identity.permissions p
WHERE r.name = 'COMPANY_MANAGER' AND p.code IN ('identity:users:read', 'identity:users:manage');
