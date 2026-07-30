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

	/** Throws {@link SupplierException} unless the supplier exists and is ACTIVE - used by Procurement before placing a purchase order. */
	void validateActiveSupplier(UUID supplierId);

	List<SupplierSummary> listSuppliers();

	List<SupplierSummary> listSuppliersForProduct(UUID productId);

	/** Product IDs this supplier is linked to sell - what an admin may pick from when raising a purchase order. */
	List<UUID> listProductIdsForSupplier(UUID supplierId);
}
