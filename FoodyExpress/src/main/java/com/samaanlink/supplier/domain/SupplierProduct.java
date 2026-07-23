package com.samaanlink.supplier.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** {@code productId} references catalogue.products by UUID only - never a JPA relationship across modules. */
@Entity
@Table(name = "supplier_products", schema = "supplier")
public class SupplierProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "supplier_id", nullable = false)
	private Supplier supplier;

	private UUID productId;

	private String supplierSku;

	protected SupplierProduct() {
	}

	public static SupplierProduct link(Supplier supplier, UUID productId, String supplierSku) {
		SupplierProduct link = new SupplierProduct();
		link.supplier = supplier;
		link.productId = productId;
		link.supplierSku = supplierSku;
		return link;
	}

	public UUID getId() {
		return id;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public UUID getProductId() {
		return productId;
	}

	public String getSupplierSku() {
		return supplierSku;
	}
}
