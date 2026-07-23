package com.samaanlink.restaurant.application;

import java.math.BigDecimal;
import java.util.UUID;

public record RestaurantSummary(UUID id, String name, BigDecimal creditLimit, int paymentTermDays, String status) {
}
