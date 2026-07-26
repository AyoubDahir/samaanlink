package com.samaanlink.pricing.application;

import java.math.BigDecimal;
import java.util.UUID;

/** Restaurant places an order for {@code quantity} units of {@code productId}; produces a persisted {@link PriceQuote}. */
public record QuoteLineCommand(UUID productId, UUID restaurantId, BigDecimal quantity) {
}
