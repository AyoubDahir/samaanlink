package com.samaanlink.catalogue.application.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samaanlink.catalogue.application.AddProductImageCommand;
import com.samaanlink.catalogue.application.CatalogueException;
import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.catalogue.application.CategorySummary;
import com.samaanlink.catalogue.application.CreateCategoryCommand;
import com.samaanlink.catalogue.application.CreateProductCommand;
import com.samaanlink.catalogue.application.ProductSummary;
import com.samaanlink.catalogue.domain.Category;
import com.samaanlink.catalogue.domain.Product;
import com.samaanlink.catalogue.domain.ProductImage;
import com.samaanlink.catalogue.domain.UnitOfMeasure;
import com.samaanlink.catalogue.infrastructure.CategoryRepository;
import com.samaanlink.catalogue.infrastructure.ProductImageRepository;
import com.samaanlink.catalogue.infrastructure.ProductRepository;
import com.samaanlink.catalogue.infrastructure.UnitOfMeasureRepository;

@Service
public class CatalogueFacadeImpl implements CatalogueFacade {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final UnitOfMeasureRepository unitOfMeasureRepository;

	public CatalogueFacadeImpl(CategoryRepository categoryRepository, ProductRepository productRepository,
			ProductImageRepository productImageRepository, UnitOfMeasureRepository unitOfMeasureRepository) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
		this.unitOfMeasureRepository = unitOfMeasureRepository;
	}

	@Override
	@Transactional
	public CategorySummary createCategory(CreateCategoryCommand command) {
		Category parent = command.parentCategoryId() == null ? null
				: categoryRepository.findById(command.parentCategoryId())
						.orElseThrow(() -> new CatalogueException("Parent category not found"));
		Category category = categoryRepository.save(Category.create(command.name(), parent));
		return toSummary(category);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CategorySummary> listCategories() {
		return categoryRepository.findAll().stream().map(this::toSummary).toList();
	}

	@Override
	@Transactional
	public void deleteCategory(UUID categoryId) {
		if (!categoryRepository.existsById(categoryId)) {
			throw new CatalogueException("Category not found");
		}
		if (productRepository.existsByCategoryId(categoryId)) {
			throw new CatalogueException("Cannot delete a category that still has products - move or delete them first");
		}
		if (categoryRepository.existsByParentCategoryId(categoryId)) {
			throw new CatalogueException("Cannot delete a category that still has subcategories - move or delete them first");
		}
		categoryRepository.deleteById(categoryId);
	}

	@Override
	@Transactional
	public ProductSummary createProduct(CreateProductCommand command) {
		if (productRepository.existsBySku(command.sku())) {
			throw new CatalogueException("A product with this SKU already exists");
		}
		Category category = categoryRepository.findById(command.categoryId())
				.orElseThrow(() -> new CatalogueException("Category not found"));
		UnitOfMeasure purchaseUnit = unitOfMeasureRepository.findByCode(command.purchaseUnitCode())
				.orElseThrow(() -> new CatalogueException("Unknown purchase unit: " + command.purchaseUnitCode()));
		UnitOfMeasure sellingUnit = unitOfMeasureRepository.findByCode(command.sellingUnitCode())
				.orElseThrow(() -> new CatalogueException("Unknown selling unit: " + command.sellingUnitCode()));

		Product product = Product.create(command.name(), command.description(), category, command.sku(),
				command.barcode(), purchaseUnit, sellingUnit, command.packageSize(), command.unitsPerPackage(),
				command.weightKg());
		product = productRepository.save(product);
		return toSummary(product);
	}

	@Override
	@Transactional
	public void deleteProduct(UUID productId) {
		if (!productRepository.existsById(productId)) {
			throw new CatalogueException("Product not found");
		}
		productImageRepository.deleteByProductId(productId);
		productRepository.deleteById(productId);
	}

	@Override
	@Transactional
	public void activateProduct(UUID productId) {
		productRepository.findById(productId).orElseThrow(() -> new CatalogueException("Product not found"))
				.activate();
	}

	@Override
	@Transactional
	public void discontinueProduct(UUID productId) {
		productRepository.findById(productId).orElseThrow(() -> new CatalogueException("Product not found"))
				.discontinue();
	}

	@Override
	@Transactional
	public void addProductImage(AddProductImageCommand command) {
		Product product = productRepository.findById(command.productId())
				.orElseThrow(() -> new CatalogueException("Product not found"));
		productImageRepository.save(ProductImage.of(product, command.url(), command.sortOrder()));
	}

	@Override
	@Transactional(readOnly = true)
	public ProductSummary findProduct(UUID productId) {
		return productRepository.findById(productId).map(this::toSummary)
				.orElseThrow(() -> new CatalogueException("Product not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductSummary> listProductsByCategory(UUID categoryId) {
		return productRepository.findByCategoryId(categoryId).stream().map(this::toSummary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductSummary> listProducts() {
		return productRepository.findAll().stream().map(this::toSummary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean productExists(UUID productId) {
		return productRepository.existsById(productId);
	}

	private CategorySummary toSummary(Category category) {
		UUID parentId = category.getParentCategory() != null ? category.getParentCategory().getId() : null;
		return new CategorySummary(category.getId(), category.getName(), parentId, category.getStatus().name());
	}

	private ProductSummary toSummary(Product product) {
		String imageUrl = productImageRepository.findByProductIdOrderBySortOrder(product.getId()).stream()
				.findFirst().map(ProductImage::getUrl).orElse(null);
		return new ProductSummary(product.getId(), product.getName(), product.getSku(), product.getBarcode(),
				product.getCategory().getId(), product.getCategory().getName(), product.getPurchaseUnit().getCode(),
				product.getSellingUnit().getCode(), product.getPackageSize(), product.getUnitsPerPackage(),
				product.getWeightKg(), product.getStatus().name(), imageUrl);
	}
}
