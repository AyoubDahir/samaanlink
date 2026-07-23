package com.samaanlink.restaurant.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCreditLimitRequest(@NotNull @Min(0) BigDecimal newLimit) {
}
