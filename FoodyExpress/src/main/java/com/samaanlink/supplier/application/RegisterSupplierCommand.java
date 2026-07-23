package com.samaanlink.supplier.application;

public record RegisterSupplierCommand(String name, int leadTimeDays, int paymentTermDays) {
}
