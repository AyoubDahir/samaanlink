package com.samaanlink.pricing.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.pricing.domain.StandardSellingPrice;

public interface StandardSellingPriceRepository extends JpaRepository<StandardSellingPrice, UUID> {
}
