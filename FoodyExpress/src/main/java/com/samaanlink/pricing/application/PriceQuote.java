package com.samaanlink.pricing.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Read model over a persisted {@link com.samaanlink.pricing.domain.PriceSnapshot}. */
public record PriceQuote(UUID id, UUID productId, UUID restaurantId, BigDecimal quantity,
		BigDecimal unitPurchasePrice, BigDecimal unitSellingPrice, BigDecimal lineSubtotal,
		BigDecimal discountAmount, BigDecimal serviceFeeAmount, BigDecimal taxAmount, BigDecimal lineTotal,
		Instant createdAt) {
}
