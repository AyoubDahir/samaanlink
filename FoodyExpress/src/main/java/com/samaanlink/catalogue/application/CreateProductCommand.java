package com.samaanlink.catalogue.application;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductCommand(
		String name,
		String description,
		UUID categoryId,
		String sku,
		String barcode,
		String purchaseUnitCode,
		String sellingUnitCode,
		BigDecimal packageSize,
		BigDecimal unitsPerPackage,
		BigDecimal weightKg) {
}
