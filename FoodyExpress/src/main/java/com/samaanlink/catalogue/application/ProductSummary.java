package com.samaanlink.catalogue.application;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummary(
		UUID id,
		String name,
		String sku,
		String barcode,
		UUID categoryId,
		String categoryName,
		String purchaseUnitCode,
		String sellingUnitCode,
		BigDecimal packageSize,
		BigDecimal unitsPerPackage,
		BigDecimal weightKg,
		String status,
		String imageUrl) {
}
