package com.samaanlink.orders.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samaanlink.orders.application.AddOrderLineCommand;
import com.samaanlink.orders.application.CreateOrderCommand;
import com.samaanlink.orders.application.OrderFacade;
import com.samaanlink.orders.application.OrderLineSummary;
import com.samaanlink.orders.application.OrderSummary;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final OrderFacade orderFacade;

	public OrderController(OrderFacade orderFacade) {
		this.orderFacade = orderFacade;
	}

	@PostMapping
	public ResponseEntity<OrderSummary> create(@Valid @RequestBody CreateOrderRequest request) {
		OrderSummary order = orderFacade
				.createOrder(new CreateOrderCommand(request.restaurantId(), request.deliveryAddressId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(order);
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderSummary> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(orderFacade.findOrder(id));
	}

	@GetMapping
	public ResponseEntity<List<OrderSummary>> list(@RequestParam(required = false) UUID restaurantId) {
		List<OrderSummary> orders = restaurantId != null ? orderFacade.listOrdersByRestaurant(restaurantId)
				: orderFacade.listAllOrders();
		return ResponseEntity.ok(orders);
	}

	@PostMapping("/{id}/lines")
	public ResponseEntity<OrderLineSummary> addLine(@PathVariable UUID id,
			@Valid @RequestBody AddOrderLineRequest request) {
		OrderLineSummary line = orderFacade.addLine(new AddOrderLineCommand(id, request.productId(), request.quantity()));
		return ResponseEntity.status(HttpStatus.CREATED).body(line);
	}

	@DeleteMapping("/{id}/lines/{lineId}")
	public ResponseEntity<Void> removeLine(@PathVariable UUID id, @PathVariable UUID lineId) {
		orderFacade.removeLine(id, lineId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/place")
	public ResponseEntity<OrderSummary> place(@PathVariable UUID id) {
		return ResponseEntity.ok(orderFacade.placeOrder(id));
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<Void> cancel(@PathVariable UUID id) {
		orderFacade.cancelOrder(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/deliver")
	public ResponseEntity<Void> deliver(@PathVariable UUID id) {
		orderFacade.markDelivered(id);
		return ResponseEntity.noContent().build();
	}
}
