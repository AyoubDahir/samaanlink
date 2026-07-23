package com.samaanlink.catalogue.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.catalogue.domain.UnitOfMeasure;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, UUID> {

	Optional<UnitOfMeasure> findByCode(String code);
}
