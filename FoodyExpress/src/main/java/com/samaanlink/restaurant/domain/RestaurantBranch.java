package com.samaanlink.restaurant.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "restaurant_branches", schema = "restaurant")
public class RestaurantBranch {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "restaurant_id", nullable = false)
	private Restaurant restaurant;

	private String name;

	private String city;

	@Column(name = "is_primary")
	private boolean primary;

	protected RestaurantBranch() {
	}

	public static RestaurantBranch create(Restaurant restaurant, String name, String city, boolean primary) {
		RestaurantBranch branch = new RestaurantBranch();
		branch.restaurant = restaurant;
		branch.name = name;
		branch.city = city;
		branch.primary = primary;
		return branch;
	}

	public UUID getId() {
		return id;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public String getName() {
		return name;
	}

	public String getCity() {
		return city;
	}

	public boolean isPrimary() {
		return primary;
	}
}
