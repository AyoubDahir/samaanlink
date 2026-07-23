package com.samaanlink.catalogue.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.catalogue.domain.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

	boolean existsBySku(String sku);

	List<Product> findByCategoryId(UUID categoryId);

	Optional<Product> findBySku(String sku);
}
