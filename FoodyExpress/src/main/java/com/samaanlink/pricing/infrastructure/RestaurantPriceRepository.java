package com.samaanlink.pricing.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.pricing.domain.RestaurantPrice;

public interface RestaurantPriceRepository extends JpaRepository<RestaurantPrice, UUID> {

	Optional<RestaurantPrice> findByProductIdAndRestaurantId(UUID productId, UUID restaurantId);
}
