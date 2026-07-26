package com.samaanlink.pricing.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.pricing.domain.ProductDiscount;

public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, UUID> {
}
