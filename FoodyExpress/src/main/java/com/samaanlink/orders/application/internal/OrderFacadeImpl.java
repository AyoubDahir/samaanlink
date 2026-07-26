package com.samaanlink.orders.application.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.orders.application.AddOrderLineCommand;
import com.samaanlink.orders.application.CreateOrderCommand;
import com.samaanlink.orders.application.OrderException;
import com.samaanlink.orders.application.OrderFacade;
import com.samaanlink.orders.application.OrderLineSummary;
import com.samaanlink.orders.application.OrderSummary;
import com.samaanlink.orders.domain.Order;
import com.samaanlink.orders.domain.OrderLine;
import com.samaanlink.orders.infrastructure.OrderLineRepository;
import com.samaanlink.orders.infrastructure.OrderRepository;
import com.samaanlink.pricing.application.PriceQuote;
import com.samaanlink.pricing.application.PricingFacade;
import com.samaanlink.pricing.application.QuoteLineCommand;
import com.samaanlink.restaurant.application.RestaurantFacade;

@Service
public class OrderFacadeImpl implements OrderFacade {

	private final OrderRepository orderRepository;
	private final OrderLineRepository orderLineRepository;
	private final CatalogueFacade catalogueFacade;
	private final RestaurantFacade restaurantFacade;
	private final PricingFacade pricingFacade;

	public OrderFacadeImpl(OrderRepository orderRepository, OrderLineRepository orderLineRepository,
			CatalogueFacade catalogueFacade, RestaurantFacade restaurantFacade, PricingFacade pricingFacade) {
		this.orderRepository = orderRepository;
		this.orderLineRepository = orderLineRepository;
		this.catalogueFacade = catalogueFacade;
		this.restaurantFacade = restaurantFacade;
		this.pricingFacade = pricingFacade;
	}

	@Override
	@Transactional
	public OrderSummary createOrder(CreateOrderCommand command) {
		restaurantFacade.validateActiveRestaurant(command.restaurantId());
		Order order = orderRepository.save(Order.createDraft(command.restaurantId(), command.deliveryAddressId()));
		return toSummary(order);
	}

	@Override
	@Transactional
	public OrderLineSummary addLine(AddOrderLineCommand command) {
		Order order = requireOrder(command.orderId());
		requireDraft(order);
		if (!catalogueFacade.productExists(command.productId())) {
			throw new OrderException("Product not found: " + command.productId());
		}

		PriceQuote quote = pricingFacade
				.quoteLine(new QuoteLineCommand(command.productId(), order.getRestaurantId(), command.quantity()));

		OrderLine line = orderLineRepository
				.save(OrderLine.of(order, command.productId(), command.quantity(), quote.id(), quote.lineTotal()));
		return toSummary(line);
	}

	@Override
	@Transactional
	public void removeLine(UUID orderId, UUID lineId) {
		Order order = requireOrder(orderId);
		requireDraft(order);
		orderLineRepository.deleteByIdAndOrderId(lineId, orderId);
	}

	@Override
	@Transactional
	public OrderSummary placeOrder(UUID orderId) {
		Order order = requireOrder(orderId);
		requireDraft(order);
		restaurantFacade.validateActiveRestaurant(order.getRestaurantId());

		List<OrderLine> lines = orderLineRepository.findByOrderId(orderId);
		if (lines.isEmpty()) {
			throw new OrderException("Cannot place an order with no lines");
		}

		BigDecimal subtotal = lines.stream().map(OrderLine::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal deliveryFee = pricingFacade.currentDeliveryFlatFee();
		order.place(subtotal, deliveryFee);
		return toSummary(order, lines);
	}

	@Override
	@Transactional
	public void cancelOrder(UUID orderId) {
		Order order = requireOrder(orderId);
		if (!order.isDraft() && !order.isPlaced()) {
			throw new OrderException("Only a DRAFT or PLACED order can be cancelled");
		}
		order.cancel();
	}

	@Override
	@Transactional
	public void markDelivered(UUID orderId) {
		Order order = requireOrder(orderId);
		if (!order.isPlaced()) {
			throw new OrderException("Only a PLACED order can be marked delivered");
		}
		order.markDelivered();
	}

	@Override
	@Transactional(readOnly = true)
	public OrderSummary findOrder(UUID orderId) {
		Order order = requireOrder(orderId);
		return toSummary(order, orderLineRepository.findByOrderId(orderId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderSummary> listOrdersByRestaurant(UUID restaurantId) {
		return orderRepository.findByRestaurantId(restaurantId).stream()
				.map(order -> toSummary(order, orderLineRepository.findByOrderId(order.getId()))).toList();
	}

	private Order requireOrder(UUID orderId) {
		return orderRepository.findById(orderId).orElseThrow(() -> new OrderException("Order not found"));
	}

	private void requireDraft(Order order) {
		if (!order.isDraft()) {
			throw new OrderException("Order is not in DRAFT status");
		}
	}

	private OrderSummary toSummary(Order order) {
		return toSummary(order, orderLineRepository.findByOrderId(order.getId()));
	}

	private OrderSummary toSummary(Order order, List<OrderLine> lines) {
		return new OrderSummary(order.getId(), order.getRestaurantId(), order.getDeliveryAddressId(),
				order.getStatus().name(), order.getSubtotal(), order.getDeliveryFee(), order.getOrderTotal(),
				order.getCreatedAt(), order.getPlacedAt(), lines.stream().map(this::toSummary).toList());
	}

	private OrderLineSummary toSummary(OrderLine line) {
		return new OrderLineSummary(line.getId(), line.getProductId(), line.getQuantity(), line.getPriceQuoteId(),
				line.getLineTotal());
	}
}
