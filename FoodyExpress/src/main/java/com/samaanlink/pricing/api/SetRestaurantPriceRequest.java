package com.samaanlink.pricing.api;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record SetRestaurantPriceRequest(@NotNull UUID restaurantId,
		@NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal price) {
}
