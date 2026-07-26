package com.samaanlink.pricing.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Keyed directly by productId (catalogue.products, UUID reference only) - one current price per product. */
@Entity
@Table(name = "purchase_prices", schema = "pricing")
public class PurchasePrice {

	@Id
	@Column(name = "product_id")
	private UUID productId;

	private BigDecimal price;

	private Instant updatedAt;

	protected PurchasePrice() {
	}

	public static PurchasePrice of(UUID productId, BigDecimal price) {
		PurchasePrice entity = new PurchasePrice();
		entity.productId = productId;
		entity.price = price;
		entity.updatedAt = Instant.now();
		return entity;
	}

	public void updatePrice(BigDecimal price) {
		this.price = price;
		this.updatedAt = Instant.now();
	}

	public UUID getProductId() {
		return productId;
	}

	public BigDecimal getPrice() {
		return price;
	}
}
