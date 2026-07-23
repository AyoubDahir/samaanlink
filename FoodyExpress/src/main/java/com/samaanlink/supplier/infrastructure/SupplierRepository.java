package com.samaanlink.supplier.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.supplier.domain.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
}
