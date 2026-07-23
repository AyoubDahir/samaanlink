package com.samaanlink.restaurant.application;

import java.util.UUID;

public record AddRestaurantContactCommand(UUID restaurantId, String name, String phone, String email,
		String roleTitle) {
}
