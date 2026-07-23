package com.samaanlink.restaurant.application;

import java.util.UUID;

public record DeliveryAddressSummary(UUID id, UUID branchId, String label, String addressLine, String city,
		boolean defaultAddress) {
}
