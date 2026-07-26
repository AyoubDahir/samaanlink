package com.samaanlink.pricing.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_discounts", schema = "pricing")
public class ProductDiscount {

	@Id
	@Column(name = "product_id")
	private UUID productId;

	private BigDecimal discountPercent;

	protected ProductDiscount() {
	}

	public static ProductDiscount of(UUID productId, BigDecimal discountPercent) {
		ProductDiscount entity = new ProductDiscount();
		entity.productId = productId;
		entity.discountPercent = discountPercent;
		return entity;
	}

	public void updateDiscountPercent(BigDecimal discountPercent) {
		this.discountPercent = discountPercent;
	}

	public UUID getProductId() {
		return productId;
	}

	public BigDecimal getDiscountPercent() {
		return discountPercent;
	}
}
