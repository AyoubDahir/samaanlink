package com.samaanlink.billing.application;

import java.util.List;
import java.util.UUID;

/** The only way another module may interact with Billing. */
public interface BillFacade {

	BillSummary generateBill(GenerateBillCommand command);

	BillSummary markPaid(UUID billId);

	BillSummary findBill(UUID billId);

	BillSummary findByOrder(UUID orderId);

	List<BillSummary> listByRestaurant(UUID restaurantId);
}
