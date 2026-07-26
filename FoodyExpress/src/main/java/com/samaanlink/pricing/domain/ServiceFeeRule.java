package com.samaanlink.pricing.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Single global rule for MVP - one row, seeded at 5% (V3_002). Per-product/per-restaurant rules are a future extension. */
@Entity
@Table(name = "service_fee_rules", schema = "pricing")
public class ServiceFeeRule {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private BigDecimal ratePercent;

	protected ServiceFeeRule() {
	}

	public void updateRatePercent(BigDecimal ratePercent) {
		this.ratePercent = ratePercent;
	}

	public UUID getId() {
		return id;
	}

	public BigDecimal getRatePercent() {
		return ratePercent;
	}
}
