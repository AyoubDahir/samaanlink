package com.samaanlink.supplier.api;

import jakarta.validation.constraints.NotBlank;

public record AddContactRequest(@NotBlank String name, String phone, String email, String roleTitle) {
}
