package com.samaanlink.restaurant.api;

import jakarta.validation.constraints.NotBlank;

public record AddBranchRequest(@NotBlank String name, String city, boolean primary) {
}
