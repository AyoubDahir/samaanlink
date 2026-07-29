package com.samaanlink.billing.application;

import java.util.UUID;

public record GenerateBillCommand(UUID orderId) {
}
