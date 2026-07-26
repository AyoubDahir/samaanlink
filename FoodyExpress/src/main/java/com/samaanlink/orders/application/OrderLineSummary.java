package com.samaanlink.orders.application;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineSummary(UUID id, UUID productId, BigDecimal quantity, UUID priceQuoteId,
		BigDecimal lineTotal) {
}
