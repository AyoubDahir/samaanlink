package com.samaanlink.supplier.application;

import java.util.UUID;

public record LinkProductCommand(UUID supplierId, UUID productId, String supplierSku) {
}
