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
@Table(name = "delivery_addresses", schema = "restaurant")
public class DeliveryAddress {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "branch_id", nullable = false)
	private RestaurantBranch branch;

	private String label;

	private String addressLine;

	private String city;

	@Column(name = "is_default")
	private boolean defaultAddress;

	protected DeliveryAddress() {
	}

	public static DeliveryAddress create(RestaurantBranch branch, String label, String addressLine, String city,
			boolean defaultAddress) {
		DeliveryAddress address = new DeliveryAddress();
		address.branch = branch;
		address.label = label;
		address.addressLine = addressLine;
		address.city = city;
		address.defaultAddress = defaultAddress;
		return address;
	}

	public UUID getId() {
		return id;
	}

	public RestaurantBranch getBranch() {
		return branch;
	}

	public String getLabel() {
		return label;
	}

	public String getAddressLine() {
		return addressLine;
	}

	public String getCity() {
		return city;
	}

	public boolean isDefaultAddress() {
		return defaultAddress;
	}
}
