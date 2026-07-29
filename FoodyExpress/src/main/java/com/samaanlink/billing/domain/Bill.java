package com.samaanlink.billing.domain;

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
 * Aggregate root for a bill issued against a single {@code PLACED} order. {@code orderId} and
 * {@code restaurantId} reference the Orders/Restaurant modules by UUID only. {@code amount} is
 * copied from the order's total at issue time and never recomputed, matching how
 * {@code Order.place()} freezes its own total from a {@code PriceSnapshot}.
 */
@Entity(name = "BillingBill")
@Table(name = "bills", schema = "billing")
public class Bill extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private UUID orderId;

	private UUID restaurantId;

	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private BillStatus status;

	private Instant paidAt;

	protected Bill() {
	}

	public static Bill issue(UUID orderId, UUID restaurantId, BigDecimal amount) {
		Bill bill = new Bill();
		bill.orderId = orderId;
		bill.restaurantId = restaurantId;
		bill.amount = amount;
		bill.status = BillStatus.ISSUED;
		return bill;
	}

	public boolean isPaid() {
		return status == BillStatus.PAID;
	}

	/** Caller (the facade) is responsible for checking {@link #isPaid()} first. */
	public void markPaid() {
		this.status = BillStatus.PAID;
		this.paidAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public UUID getRestaurantId() {
		return restaurantId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BillStatus getStatus() {
		return status;
	}

	public Instant getPaidAt() {
		return paidAt;
	}
}
