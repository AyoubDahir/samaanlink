package com.samaanlink.billing.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.billing.domain.Bill;

public interface BillRepository extends JpaRepository<Bill, UUID> {

	boolean existsByOrderId(UUID orderId);

	Optional<Bill> findByOrderId(UUID orderId);

	List<Bill> findByRestaurantId(UUID restaurantId);
}
