package com.samaanlink.restaurant.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.restaurant.domain.RestaurantContact;

public interface RestaurantContactRepository extends JpaRepository<RestaurantContact, UUID> {

	List<RestaurantContact> findByRestaurantId(UUID restaurantId);
}
