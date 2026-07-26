package com.samaanlink.pricing.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** An override of {@link StandardSellingPrice} for one specific restaurant; absence means the standard price applies. */
@Entity
@Table(name = "restaurant_prices", schema = "pricing")
public class RestaurantPrice {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private UUID productId;

	private UUID restaurantId;

	private BigDecimal price;

	private Instant updatedAt;

	protected RestaurantPrice() {
	}

	public static RestaurantPrice of(UUID productId, UUID restaurantId, BigDecimal price) {
		RestaurantPrice entity = new RestaurantPrice();
		entity.productId = productId;
		entity.restaurantId = restaurantId;
		entity.price = price;
		entity.updatedAt = Instant.now();
		return entity;
	}

	public void updatePrice(BigDecimal price) {
		this.price = price;
		this.updatedAt = Instant.now();
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

	public BigDecimal getPrice() {
		return price;
	}
}
