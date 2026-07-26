-- Defaults matching the architecture doc's worked example: 5% procurement service fee, $1.00 flat delivery fee.
INSERT INTO pricing.service_fee_rules (id, rate_percent) VALUES (gen_random_uuid(), 5.00);
INSERT INTO pricing.delivery_fee_rules (id, flat_fee) VALUES (gen_random_uuid(), 1.00);
