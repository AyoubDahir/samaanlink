package com.samaanlink.orders.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** {@code lineTotal} is copied from the {@code PriceQuote} at the moment the line was added, not recomputed later. */
@Entity
@Table(name = "order_lines", schema = "orders")
public class OrderLine {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	private UUID productId;

	private BigDecimal quantity;

	private UUID priceQuoteId;

	private BigDecimal lineTotal;

	protected OrderLine() {
	}

	public static OrderLine of(Order order, UUID productId, BigDecimal quantity, UUID priceQuoteId,
			BigDecimal lineTotal) {
		OrderLine line = new OrderLine();
		line.order = order;
		line.productId = productId;
		line.quantity = quantity;
		line.priceQuoteId = priceQuoteId;
		line.lineTotal = lineTotal;
		return line;
	}

	public UUID getId() {
		return id;
	}

	public Order getOrder() {
		return order;
	}

	public UUID getProductId() {
		return productId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public UUID getPriceQuoteId() {
		return priceQuoteId;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}
}
