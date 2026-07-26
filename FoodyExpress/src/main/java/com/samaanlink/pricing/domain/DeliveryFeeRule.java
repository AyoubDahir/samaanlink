package com.samaanlink.pricing.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Single global flat fee for MVP - one row, seeded at $1.00 (V3_002). Per-branch/distance-based fees are a future extension. */
@Entity
@Table(name = "delivery_fee_rules", schema = "pricing")
public class DeliveryFeeRule {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private BigDecimal flatFee;

	protected DeliveryFeeRule() {
	}

	public void updateFlatFee(BigDecimal flatFee) {
		this.flatFee = flatFee;
	}

	public UUID getId() {
		return id;
	}

	public BigDecimal getFlatFee() {
		return flatFee;
	}
}
