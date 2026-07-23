package com.samaanlink.catalogue.api;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(@NotBlank String name, UUID parentCategoryId) {
}
