package com.samaanlink.procurement.application;

import java.util.UUID;

public record CreatePurchaseOrderCommand(UUID supplierId) {
}
