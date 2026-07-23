package com.samaanlink.supplier.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.supplier.domain.SupplierContact;

public interface SupplierContactRepository extends JpaRepository<SupplierContact, UUID> {

	List<SupplierContact> findBySupplierId(UUID supplierId);
}
