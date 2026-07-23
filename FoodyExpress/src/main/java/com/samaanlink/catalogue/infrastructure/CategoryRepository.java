package com.samaanlink.catalogue.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.catalogue.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
