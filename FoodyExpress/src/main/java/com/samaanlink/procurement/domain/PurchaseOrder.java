package com.samaanlink.procurement.domain;

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
 * Aggregate root for a purchase order the platform raises against a wholesaler/supplier -
 * structurally the mirror of {@code orders.Order} (restaurant buying from the platform), but on
 * the buy side. {@code supplierId} references the Supplier module by UUID only. Lines may only be
 * added/removed while {@link PurchaseOrderStatus#DRAFT}; {@link #place} freezes the subtotal, and
 * {@link #receive} records that the goods physically arrived - there is no inventory/stock ledger
 * behind this, it is a purchase record only.
 */
@Entity
@Table(name = "purchase_orders", schema = "procurement")
public class PurchaseOrder extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private UUID supplierId;

	@Enumerated(EnumType.STRING)
	private PurchaseOrderStatus status;

	private BigDecimal subtotal;

	private Instant placedAt;

	private Instant receivedAt;

	protected PurchaseOrder() {
	}

	public static PurchaseOrder createDraft(UUID supplierId) {
		PurchaseOrder purchaseOrder = new PurchaseOrder();
		purchaseOrder.supplierId = supplierId;
		purchaseOrder.status = PurchaseOrderStatus.DRAFT;
		return purchaseOrder;
	}

	public boolean isDraft() {
		return status == PurchaseOrderStatus.DRAFT;
	}

	public boolean isPlaced() {
		return status == PurchaseOrderStatus.PLACED;
	}

	/** Caller (the facade) is responsible for checking {@link #isDraft()} first. */
	public void place(BigDecimal subtotal) {
		this.subtotal = subtotal;
		this.status = PurchaseOrderStatus.PLACED;
		this.placedAt = Instant.now();
	}

	/** Caller (the facade) is responsible for checking {@link #isPlaced()} first. */
	public void receive() {
		this.status = PurchaseOrderStatus.RECEIVED;
		this.receivedAt = Instant.now();
	}

	public void cancel() {
		this.status = PurchaseOrderStatus.CANCELLED;
	}

	public UUID getId() {
		return id;
	}

	public UUID getSupplierId() {
		return supplierId;
	}

	public PurchaseOrderStatus getStatus() {
		return status;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public Instant getPlacedAt() {
		return placedAt;
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}
}
