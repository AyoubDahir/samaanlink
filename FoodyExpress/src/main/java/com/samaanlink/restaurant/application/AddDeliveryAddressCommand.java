package com.samaanlink.restaurant.application;

import java.util.UUID;

public record AddDeliveryAddressCommand(UUID branchId, String label, String addressLine, String city,
		boolean defaultAddress) {
}
