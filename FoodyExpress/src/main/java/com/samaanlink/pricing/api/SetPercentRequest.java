package com.samaanlink.pricing.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record SetPercentRequest(@NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal percent) {
}
