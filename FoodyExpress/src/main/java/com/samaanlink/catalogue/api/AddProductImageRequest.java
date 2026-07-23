package com.samaanlink.catalogue.api;

import jakarta.validation.constraints.NotBlank;

public record AddProductImageRequest(@NotBlank String url, int sortOrder) {
}
