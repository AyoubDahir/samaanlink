package com.samaanlink.pricing.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_taxes", schema = "pricing")
public class ProductTax {

	@Id
	@Column(name = "product_id")
	private UUID productId;

	private BigDecimal taxPercent;

	protected ProductTax() {
	}

	public static ProductTax of(UUID productId, BigDecimal taxPercent) {
		ProductTax entity = new ProductTax();
		entity.productId = productId;
		entity.taxPercent = taxPercent;
		return entity;
	}

	public void updateTaxPercent(BigDecimal taxPercent) {
		this.taxPercent = taxPercent;
	}

	public UUID getProductId() {
		return productId;
	}

	public BigDecimal getTaxPercent() {
		return taxPercent;
	}
}
