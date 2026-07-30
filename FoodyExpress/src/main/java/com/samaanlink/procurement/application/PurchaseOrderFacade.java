package com.samaanlink.procurement.application;

import java.util.List;
import java.util.UUID;

/** The only way another module may interact with Procurement. */
public interface PurchaseOrderFacade {

	PurchaseOrderSummary createPurchaseOrder(CreatePurchaseOrderCommand command);

	PurchaseOrderLineSummary addLine(AddPurchaseOrderLineCommand command);

	void removeLine(UUID purchaseOrderId, UUID lineId);

	PurchaseOrderSummary placeOrder(UUID purchaseOrderId);

	PurchaseOrderSummary receiveOrder(UUID purchaseOrderId);

	void cancelOrder(UUID purchaseOrderId);

	PurchaseOrderSummary findPurchaseOrder(UUID purchaseOrderId);

	List<PurchaseOrderSummary> listAllPurchaseOrders();

	List<PurchaseOrderSummary> listPurchaseOrdersBySupplier(UUID supplierId);
}
