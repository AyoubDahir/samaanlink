package com.samaanlink.restaurant.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.restaurant.domain.RestaurantBranch;

public interface RestaurantBranchRepository extends JpaRepository<RestaurantBranch, UUID> {

	List<RestaurantBranch> findByRestaurantId(UUID restaurantId);
}
