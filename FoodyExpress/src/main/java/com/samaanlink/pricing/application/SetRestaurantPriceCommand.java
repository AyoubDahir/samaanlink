package com.samaanlink.pricing.application;

import java.math.BigDecimal;
import java.util.UUID;

public record SetRestaurantPriceCommand(UUID productId, UUID restaurantId, BigDecimal price) {
}
