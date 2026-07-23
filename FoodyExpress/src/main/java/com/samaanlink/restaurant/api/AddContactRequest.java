package com.samaanlink.restaurant.api;

import jakarta.validation.constraints.NotBlank;

public record AddContactRequest(@NotBlank String name, String phone, String email, String roleTitle) {
}
