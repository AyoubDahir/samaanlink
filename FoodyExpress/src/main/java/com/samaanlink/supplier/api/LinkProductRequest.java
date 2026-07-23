package com.samaanlink.supplier.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record LinkProductRequest(@NotNull UUID productId, String supplierSku) {
}
