package com.samaanlink.orders.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.orders.domain.OrderLine;

public interface OrderLineRepository extends JpaRepository<OrderLine, UUID> {

	List<OrderLine> findByOrderId(UUID orderId);

	void deleteByIdAndOrderId(UUID id, UUID orderId);
}
