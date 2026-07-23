package com.samaanlink.catalogue.api;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateProductRequest(
		@NotBlank String name,
		String description,
		@NotNull UUID categoryId,
		@NotBlank String sku,
		String barcode,
		@NotBlank String purchaseUnitCode,
		@NotBlank String sellingUnitCode,
		@NotNull @Positive BigDecimal packageSize,
		@NotNull @Positive BigDecimal unitsPerPackage,
		BigDecimal weightKg) {
}
