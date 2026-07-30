package com.samaanlink.procurement.api;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AddPurchaseOrderLineRequest(@NotNull UUID productId,
		@NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantity,
		@NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal unitCost) {
}
