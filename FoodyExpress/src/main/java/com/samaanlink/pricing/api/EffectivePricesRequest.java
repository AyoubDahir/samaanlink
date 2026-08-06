package com.samaanlink.pricing.api;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record EffectivePricesRequest(@NotEmpty List<UUID> productIds, @NotNull UUID restaurantId) {
}
