package com.samaanlink.orders.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderSummary(UUID id, UUID restaurantId, UUID deliveryAddressId, String status, BigDecimal subtotal,
		BigDecimal deliveryFee, BigDecimal orderTotal, Instant createdAt, Instant placedAt,
		List<OrderLineSummary> lines) {
}
