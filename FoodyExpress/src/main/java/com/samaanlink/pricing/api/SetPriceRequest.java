package com.samaanlink.pricing.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record SetPriceRequest(@NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal price) {
}
