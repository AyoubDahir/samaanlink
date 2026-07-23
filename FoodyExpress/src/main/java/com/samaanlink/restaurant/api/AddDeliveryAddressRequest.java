package com.samaanlink.restaurant.api;

import jakarta.validation.constraints.NotBlank;

public record AddDeliveryAddressRequest(String label, @NotBlank String addressLine, String city,
		boolean defaultAddress) {
}
