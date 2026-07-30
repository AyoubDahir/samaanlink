package com.samaanlink.procurement.application;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderLineSummary(UUID id, UUID productId, BigDecimal quantity, BigDecimal unitCost,
		BigDecimal lineTotal) {
}
