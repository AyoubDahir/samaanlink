package com.samaanlink.pricing.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.pricing.domain.PriceSnapshot;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, UUID> {
}
