package com.samaanlink.catalogue.application;

import java.util.List;
import java.util.UUID;

/**
 * The only way another module may interact with the Catalogue. Pricing, Inventory, Procurement and
 * Orders all reference products by {@code productId} (UUID) and resolve details through this
 * facade rather than the {@code Product} entity itself.
 */
public interface CatalogueFacade {

	CategorySummary createCategory(CreateCategoryCommand command);

	ProductSummary createProduct(CreateProductCommand command);

	void activateProduct(UUID productId);

	void discontinueProduct(UUID productId);

	void addProductImage(AddProductImageCommand command);

	ProductSummary findProduct(UUID productId);

	List<ProductSummary> listProductsByCategory(UUID categoryId);

	boolean productExists(UUID productId);
}
