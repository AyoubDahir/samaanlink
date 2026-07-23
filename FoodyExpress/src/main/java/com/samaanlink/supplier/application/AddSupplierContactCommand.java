package com.samaanlink.supplier.application;

import java.util.UUID;

public record AddSupplierContactCommand(UUID supplierId, String name, String phone, String email, String roleTitle) {
}
