package com.samaanlink.restaurant.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRestaurantRequest(
		@NotBlank String restaurantName,
		@NotNull @Min(0) BigDecimal creditLimit,
		@Min(0) int paymentTermDays,
		@NotBlank String primaryBranchName,
		String primaryBranchCity,
		@NotBlank @Email String ownerEmail,
		@NotBlank @Size(min = 8, max = 128) String ownerPassword,
		@NotBlank String ownerFirstName,
		@NotBlank String ownerLastName,
		String ownerPhone) {
}
