package com.samaanlink.procurement.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreatePurchaseOrderRequest(@NotNull UUID supplierId) {
}
