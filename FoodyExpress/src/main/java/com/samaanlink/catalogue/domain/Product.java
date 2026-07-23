package com.samaanlink.catalogue.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.samaanlink.common.audit.Auditable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * {@code packageSize} is the quantity in the purchase unit that makes up one purchasable package
 * (e.g. a 40kg banana package: purchaseUnit=KG, packageSize=40). {@code unitsPerPackage} is how
 * many selling units that package yields, letting purchase and selling units differ (e.g. a
 * package bought as one 40kg bag but sold to restaurants in 1kg selling units: unitsPerPackage=40).
 */
@Entity
@Table(name = "products", schema = "catalogue")
public class Product extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String name;

	private String description;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	private String sku;

	private String barcode;

	@ManyToOne
	@JoinColumn(name = "purchase_unit_id", nullable = false)
	private UnitOfMeasure purchaseUnit;

	@ManyToOne
	@JoinColumn(name = "selling_unit_id", nullable = false)
	private UnitOfMeasure sellingUnit;

	private BigDecimal packageSize;

	private BigDecimal unitsPerPackage;

	private BigDecimal weightKg;

	@Enumerated(EnumType.STRING)
	private ProductStatus status;

	protected Product() {
	}

	public static Product create(String name, String description, Category category, String sku, String barcode,
			UnitOfMeasure purchaseUnit, UnitOfMeasure sellingUnit, BigDecimal packageSize,
			BigDecimal unitsPerPackage, BigDecimal weightKg) {
		Product product = new Product();
		product.name = name;
		product.description = description;
		product.category = category;
		product.sku = sku;
		product.barcode = barcode;
		product.purchaseUnit = purchaseUnit;
		product.sellingUnit = sellingUnit;
		product.packageSize = packageSize;
		product.unitsPerPackage = unitsPerPackage;
		product.weightKg = weightKg;
		product.status = ProductStatus.DRAFT;
		return product;
	}

	public void activate() {
		this.status = ProductStatus.ACTIVE;
	}

	public void discontinue() {
		this.status = ProductStatus.DISCONTINUED;
	}

	public boolean isActive() {
		return status == ProductStatus.ACTIVE;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Category getCategory() {
		return category;
	}

	public String getSku() {
		return sku;
	}

	public String getBarcode() {
		return barcode;
	}

	public UnitOfMeasure getPurchaseUnit() {
		return purchaseUnit;
	}

	public UnitOfMeasure getSellingUnit() {
		return sellingUnit;
	}

	public BigDecimal getPackageSize() {
		return packageSize;
	}

	public BigDecimal getUnitsPerPackage() {
		return unitsPerPackage;
	}

	public BigDecimal getWeightKg() {
		return weightKg;
	}

	public ProductStatus getStatus() {
		return status;
	}
}
