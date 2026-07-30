package com.samaanlink.procurement.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.procurement.domain.PurchaseOrderLine;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, UUID> {

	List<PurchaseOrderLine> findByPurchaseOrderId(UUID purchaseOrderId);

	void deleteByIdAndPurchaseOrderId(UUID id, UUID purchaseOrderId);
}
