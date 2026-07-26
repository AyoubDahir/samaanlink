package com.samaanlink.orders.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.samaanlink.common.audit.Auditable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Aggregate root for a restaurant's purchase order against the platform. {@code restaurantId} and
 * {@code deliveryAddressId} reference the Restaurant module by UUID only. Lines may only be
 * added/removed while {@link OrderStatus#DRAFT}; {@link #place} freezes the subtotal/total.
 */
@Entity
@Table(name = "orders", schema = "orders")
public class Order extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private UUID restaurantId;

	private UUID deliveryAddressId;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private BigDecimal subtotal;

	private BigDecimal deliveryFee;

	private BigDecimal orderTotal;

	private Instant placedAt;

	protected Order() {
	}

	public static Order createDraft(UUID restaurantId, UUID deliveryAddressId) {
		Order order = new Order();
		order.restaurantId = restaurantId;
		order.deliveryAddressId = deliveryAddressId;
		order.status = OrderStatus.DRAFT;
		return order;
	}

	public boolean isDraft() {
		return status == OrderStatus.DRAFT;
	}

	public boolean isPlaced() {
		return status == OrderStatus.PLACED;
	}

	/** Caller (the facade) is responsible for checking {@link #isDraft()} first. */
	public void place(BigDecimal subtotal, BigDecimal deliveryFee) {
		this.subtotal = subtotal;
		this.deliveryFee = deliveryFee;
		this.orderTotal = subtotal.add(deliveryFee);
		this.status = OrderStatus.PLACED;
		this.placedAt = Instant.now();
	}

	public void cancel() {
		this.status = OrderStatus.CANCELLED;
	}

	public void markDelivered() {
		this.status = OrderStatus.DELIVERED;
	}

	public UUID getId() {
		return id;
	}

	public UUID getRestaurantId() {
		return restaurantId;
	}

	public UUID getDeliveryAddressId() {
		return deliveryAddressId;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public BigDecimal getDeliveryFee() {
		return deliveryFee;
	}

	public BigDecimal getOrderTotal() {
		return orderTotal;
	}

	public Instant getPlacedAt() {
		return placedAt;
	}
}
