package com.samaanlink.restaurant.application;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateCreditLimitCommand(UUID restaurantId, BigDecimal newLimit) {
}
