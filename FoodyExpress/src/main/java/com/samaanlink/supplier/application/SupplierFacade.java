package com.samaanlink.supplier.application;

import java.util.List;
import java.util.UUID;

public interface SupplierFacade {

	SupplierSummary registerSupplier(RegisterSupplierCommand command);

	void addContact(AddSupplierContactCommand command);

	void linkProduct(LinkProductCommand command);

	void activateSupplier(UUID supplierId);

	void suspendSupplier(UUID supplierId);

	SupplierSummary findSupplier(UUID supplierId);

	List<SupplierSummary> listSuppliersForProduct(UUID productId);
}
