package com.samaanlink.pricing.application;

import java.math.BigDecimal;

public record UpdateServiceFeeRateCommand(BigDecimal ratePercent) {
}
