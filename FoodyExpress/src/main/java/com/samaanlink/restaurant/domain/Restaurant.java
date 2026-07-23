package com.samaanlink.restaurant.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.samaanlink.common.audit.Auditable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Explicit JPA entity name: the legacy com.foodyexpress.model.Restaurant entity (still live
// during the migration bridge) would otherwise collide on Hibernate's default same-simple-name
// entity registration within the persistence unit.
@Entity(name = "RestaurantAggregate")
@Table(name = "restaurants", schema = "restaurant")
public class Restaurant extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String name;

	private BigDecimal creditLimit;

	private int paymentTermDays;

	@Enumerated(EnumType.STRING)
	private RestaurantStatus status;

	protected Restaurant() {
	}

	public static Restaurant create(String name, BigDecimal creditLimit, int paymentTermDays) {
		Restaurant restaurant = new Restaurant();
		restaurant.name = name;
		restaurant.creditLimit = creditLimit;
		restaurant.paymentTermDays = paymentTermDays;
		restaurant.status = RestaurantStatus.PENDING_APPROVAL;
		return restaurant;
	}

	public void activate() {
		this.status = RestaurantStatus.ACTIVE;
	}

	public void suspend() {
		this.status = RestaurantStatus.SUSPENDED;
	}

	public void close() {
		this.status = RestaurantStatus.CLOSED;
	}

	public void updateCreditLimit(BigDecimal newLimit) {
		this.creditLimit = newLimit;
	}

	public boolean isActive() {
		return status == RestaurantStatus.ACTIVE;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getCreditLimit() {
		return creditLimit;
	}

	public int getPaymentTermDays() {
		return paymentTermDays;
	}

	public RestaurantStatus getStatus() {
		return status;
	}
}
