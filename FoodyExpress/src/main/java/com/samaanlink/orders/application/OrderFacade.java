package com.samaanlink.orders.application;

import java.util.List;
import java.util.UUID;

/** The only way another module may interact with Orders. */
public interface OrderFacade {

	OrderSummary createOrder(CreateOrderCommand command);

	OrderLineSummary addLine(AddOrderLineCommand command);

	void removeLine(UUID orderId, UUID lineId);

	OrderSummary placeOrder(UUID orderId);

	void cancelOrder(UUID orderId);

	void markDelivered(UUID orderId);

	OrderSummary findOrder(UUID orderId);

	List<OrderSummary> listOrdersByRestaurant(UUID restaurantId);

	List<OrderSummary> listAllOrders();
}
