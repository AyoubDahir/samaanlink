package com.samaanlink.procurement.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderSummary(UUID id, UUID supplierId, String status, BigDecimal subtotal, Instant createdAt,
		Instant placedAt, Instant receivedAt, List<PurchaseOrderLineSummary> lines) {
}
