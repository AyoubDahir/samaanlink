package com.samaanlink.restaurant.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** {@code userId} references identity.users(id) by UUID only - never a JPA relationship across modules. */
@Entity
@Table(name = "restaurant_users", schema = "restaurant")
public class RestaurantUser {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "restaurant_id", nullable = false)
	private Restaurant restaurant;

	private UUID userId;

	private Instant addedAt;

	protected RestaurantUser() {
	}

	public static RestaurantUser link(Restaurant restaurant, UUID userId) {
		RestaurantUser link = new RestaurantUser();
		link.restaurant = restaurant;
		link.userId = userId;
		link.addedAt = Instant.now();
		return link;
	}

	public UUID getId() {
		return id;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public UUID getUserId() {
		return userId;
	}

	public Instant getAddedAt() {
		return addedAt;
	}
}
