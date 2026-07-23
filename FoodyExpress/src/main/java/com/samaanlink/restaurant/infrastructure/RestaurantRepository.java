package com.samaanlink.restaurant.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.restaurant.domain.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
}
