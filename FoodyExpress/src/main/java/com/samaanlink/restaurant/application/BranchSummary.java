package com.samaanlink.restaurant.application;

import java.util.UUID;

public record BranchSummary(UUID id, UUID restaurantId, String name, String city, boolean primary) {
}
