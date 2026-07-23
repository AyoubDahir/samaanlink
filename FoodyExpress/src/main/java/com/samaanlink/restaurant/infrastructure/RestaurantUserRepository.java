package com.samaanlink.restaurant.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.restaurant.domain.RestaurantUser;

public interface RestaurantUserRepository extends JpaRepository<RestaurantUser, UUID> {

	Optional<RestaurantUser> findByUserId(UUID userId);
}
