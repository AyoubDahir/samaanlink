package com.samaanlink.pricing.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "standard_selling_prices", schema = "pricing")
public class StandardSellingPrice {

	@Id
	@Column(name = "product_id")
	private UUID productId;

	private BigDecimal price;

	private Instant updatedAt;

	protected StandardSellingPrice() {
	}

	public static StandardSellingPrice of(UUID productId, BigDecimal price) {
		StandardSellingPrice entity = new StandardSellingPrice();
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
