package com.samaanlink.orders.application;

import java.util.UUID;

public record CreateOrderCommand(UUID restaurantId, UUID deliveryAddressId) {
}
