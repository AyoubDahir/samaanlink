package com.samaanlink.orders.application;

import java.math.BigDecimal;
import java.util.UUID;

public record AddOrderLineCommand(UUID orderId, UUID productId, BigDecimal quantity) {
}
