package com.samaanlink.pricing.application;

import java.math.BigDecimal;
import java.util.UUID;

public record SetStandardSellingPriceCommand(UUID productId, BigDecimal price) {
}
