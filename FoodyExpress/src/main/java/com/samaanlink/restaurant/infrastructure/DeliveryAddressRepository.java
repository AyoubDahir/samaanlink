package com.samaanlink.restaurant.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.restaurant.domain.DeliveryAddress;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, UUID> {

	List<DeliveryAddress> findByBranchId(UUID branchId);
}
