package com.samaanlink.billing.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record GenerateBillRequest(@NotNull UUID orderId) {
}
