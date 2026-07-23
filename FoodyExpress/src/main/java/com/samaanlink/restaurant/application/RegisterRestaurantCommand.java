package com.samaanlink.restaurant.application;

import java.math.BigDecimal;

public record RegisterRestaurantCommand(
		String restaurantName,
		BigDecimal creditLimit,
		int paymentTermDays,
		String primaryBranchName,
		String primaryBranchCity,
		String ownerEmail,
		String ownerPassword,
		String ownerFirstName,
		String ownerLastName,
		String ownerPhone) {
}
