package com.samaanlink.procurement.application;

import java.math.BigDecimal;
import java.util.UUID;

public record AddPurchaseOrderLineCommand(UUID purchaseOrderId, UUID productId, BigDecimal quantity,
		BigDecimal unitCost) {
}
