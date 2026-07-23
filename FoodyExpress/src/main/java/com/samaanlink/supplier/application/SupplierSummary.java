package com.samaanlink.supplier.application;

import java.util.UUID;

public record SupplierSummary(UUID id, String name, int leadTimeDays, int paymentTermDays, String status) {
}
