package com.samaanlink.billing.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BillSummary(UUID id, UUID orderId, UUID restaurantId, BigDecimal amount, String status,
		Instant issuedAt, Instant paidAt) {
}
