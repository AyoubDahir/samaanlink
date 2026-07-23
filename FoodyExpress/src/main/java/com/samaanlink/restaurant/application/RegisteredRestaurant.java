package com.samaanlink.restaurant.application;

import java.util.UUID;

/** Result of {@code registerRestaurant}: the new restaurant plus the owner account created alongside it. */
public record RegisteredRestaurant(RestaurantSummary restaurant, UUID ownerUserId, UUID primaryBranchId) {
}
