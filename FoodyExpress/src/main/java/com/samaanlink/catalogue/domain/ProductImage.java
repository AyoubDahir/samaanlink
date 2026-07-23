package com.samaanlink.catalogue.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_images", schema = "catalogue")
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	private String url;

	private int sortOrder;

	protected ProductImage() {
	}

	public static ProductImage of(Product product, String url, int sortOrder) {
		ProductImage image = new ProductImage();
		image.product = product;
		image.url = url;
		image.sortOrder = sortOrder;
		return image;
	}

	public UUID getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
