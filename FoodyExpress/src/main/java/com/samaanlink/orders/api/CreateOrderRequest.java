package com.samaanlink.orders.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(@NotNull UUID restaurantId, @NotNull UUID deliveryAddressId) {
}
