package com.samaanlink.pricing.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Immutable record of a fully-calculated line price at one point in time. Never updated after
 * creation - Orders stores its id and re-fetches it rather than recomputing, so what the customer
 * was charged always matches what they were quoted, even if prices change afterward.
 */
@Entity
@Table(name = "price_snapshots", schema = "pricing")
public class PriceSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private UUID productId;

	private UUID restaurantId;

	private BigDecimal quantity;

	private BigDecimal unitPurchasePrice;

	private BigDecimal unitSellingPrice;

	private BigDecimal lineSubtotal;

	private BigDecimal discountAmount;

	private BigDecimal serviceFeeAmount;

	private BigDecimal taxAmount;

	private BigDecimal lineTotal;

	private Instant createdAt;

	protected PriceSnapshot() {
	}

	public static PriceSnapshot create(UUID productId, UUID restaurantId, BigDecimal quantity,
			BigDecimal unitPurchasePrice, BigDecimal unitSellingPrice, BigDecimal lineSubtotal,
			BigDecimal discountAmount, BigDecimal serviceFeeAmount, BigDecimal taxAmount, BigDecimal lineTotal) {
		PriceSnapshot snapshot = new PriceSnapshot();
		snapshot.productId = productId;
		snapshot.restaurantId = restaurantId;
		snapshot.quantity = quantity;
		snapshot.unitPurchasePrice = unitPurchasePrice;
		snapshot.unitSellingPrice = unitSellingPrice;
		snapshot.lineSubtotal = lineSubtotal;
		snapshot.discountAmount = discountAmount;
		snapshot.serviceFeeAmount = serviceFeeAmount;
		snapshot.taxAmount = taxAmount;
		snapshot.lineTotal = lineTotal;
		snapshot.createdAt = Instant.now();
		return snapshot;
	}

	public UUID getId() {
		return id;
	}

	public UUID getProductId() {
		return productId;
	}

	public UUID getRestaurantId() {
		return restaurantId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPurchasePrice() {
		return unitPurchasePrice;
	}

	public BigDecimal getUnitSellingPrice() {
		return unitSellingPrice;
	}

	public BigDecimal getLineSubtotal() {
		return lineSubtotal;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public BigDecimal getServiceFeeAmount() {
		return serviceFeeAmount;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
