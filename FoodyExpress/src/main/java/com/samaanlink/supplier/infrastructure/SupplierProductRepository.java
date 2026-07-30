package com.samaanlink.supplier.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.supplier.domain.SupplierProduct;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, UUID> {

	List<SupplierProduct> findByProductId(UUID productId);

	List<SupplierProduct> findBySupplierId(UUID supplierId);

	boolean existsBySupplierIdAndProductId(UUID supplierId, UUID productId);
}
