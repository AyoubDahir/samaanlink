package com.samaanlink.catalogue.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samaanlink.catalogue.domain.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

	List<ProductImage> findByProductIdOrderBySortOrder(UUID productId);
}
