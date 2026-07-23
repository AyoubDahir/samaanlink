package com.samaanlink.restaurant.application;

import java.util.UUID;

public record AddBranchCommand(UUID restaurantId, String name, String city, boolean primary) {
}
