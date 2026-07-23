package com.samaanlink.supplier.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RegisterSupplierRequest(
		@NotBlank String name,
		@Min(0) int leadTimeDays,
		@Min(0) int paymentTermDays) {
}
