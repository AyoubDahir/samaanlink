package com.samaanlink.procurement.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * {@code productId} references catalogue.products by UUID only. {@code unitCost} is what was
 * actually agreed with the supplier for this purchase - it is entered by the buyer, not derived
 * from Pricing's purchase price (which is the platform's own re-sale cost basis, set separately,
 * possibly from a past purchase).
 */
@Entity
@Table(name = "purchase_order_lines", schema = "procurement")
public class PurchaseOrderLine {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "purchase_order_id", nullable = false)
	private PurchaseOrder purchaseOrder;

	private UUID productId;

	private BigDecimal quantity;

	private BigDecimal unitCost;

	private BigDecimal lineTotal;

	protected PurchaseOrderLine() {
	}

	public static PurchaseOrderLine of(PurchaseOrder purchaseOrder, UUID productId, BigDecimal quantity,
			BigDecimal unitCost) {
		PurchaseOrderLine line = new PurchaseOrderLine();
		line.purchaseOrder = purchaseOrder;
		line.productId = productId;
		line.quantity = quantity;
		line.unitCost = unitCost;
		line.lineTotal = unitCost.multiply(quantity);
		return line;
	}

	public UUID getId() {
		return id;
	}

	public PurchaseOrder getPurchaseOrder() {
		return purchaseOrder;
	}

	public UUID getProductId() {
		return productId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitCost() {
		return unitCost;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}
}
