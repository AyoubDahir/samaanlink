package com.samaanlink.catalogue.application;

import java.util.UUID;

public record CreateCategoryCommand(String name, UUID parentCategoryId) {
}
