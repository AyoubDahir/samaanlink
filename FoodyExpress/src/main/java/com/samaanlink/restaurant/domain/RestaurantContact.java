package com.samaanlink.restaurant.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "restaurant_contacts", schema = "restaurant")
public class RestaurantContact {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "restaurant_id", nullable = false)
	private Restaurant restaurant;

	private String name;

	private String phone;

	private String email;

	private String roleTitle;

	protected RestaurantContact() {
	}

	public static RestaurantContact create(Restaurant restaurant, String name, String phone, String email,
			String roleTitle) {
		RestaurantContact contact = new RestaurantContact();
		contact.restaurant = restaurant;
		contact.name = name;
		contact.phone = phone;
		contact.email = email;
		contact.roleTitle = roleTitle;
		return contact;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public String getRoleTitle() {
		return roleTitle;
	}
}
