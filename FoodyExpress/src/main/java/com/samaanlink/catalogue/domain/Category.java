package com.samaanlink.catalogue.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Explicit JPA entity name: the legacy com.foodyexpress.model.Category entity (still live during
// the migration bridge) would otherwise collide on Hibernate's default same-simple-name entity
// registration within the persistence unit.
@Entity(name = "CatalogueCategory")
@Table(name = "categories", schema = "catalogue")
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String name;

	@ManyToOne
	@JoinColumn(name = "parent_category_id")
	private Category parentCategory;

	@Enumerated(EnumType.STRING)
	private CategoryStatus status;

	protected Category() {
	}

	public static Category create(String name, Category parentCategory) {
		Category category = new Category();
		category.name = name;
		category.parentCategory = parentCategory;
		category.status = CategoryStatus.ACTIVE;
		return category;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Category getParentCategory() {
		return parentCategory;
	}

	public CategoryStatus getStatus() {
		return status;
	}
}
