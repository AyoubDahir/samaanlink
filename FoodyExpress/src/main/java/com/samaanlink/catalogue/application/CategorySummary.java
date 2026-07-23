package com.samaanlink.catalogue.application;

import java.util.UUID;

public record CategorySummary(UUID id, String name, UUID parentCategoryId, String status) {
}
