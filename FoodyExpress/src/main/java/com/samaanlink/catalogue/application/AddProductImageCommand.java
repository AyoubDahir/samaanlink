package com.samaanlink.catalogue.application;

import java.util.UUID;

public record AddProductImageCommand(UUID productId, String url, int sortOrder) {
}
