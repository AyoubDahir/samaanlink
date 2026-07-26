package com.samaanlink.pricing.application;

import java.math.BigDecimal;
import java.util.UUID;

public record SetProductTaxCommand(UUID productId, BigDecimal taxPercent) {
}
