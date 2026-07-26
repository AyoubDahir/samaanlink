package com.samaanlink.orders.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.orders.domain.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {

	List<Order> findByRestaurantId(UUID restaurantId);
}
