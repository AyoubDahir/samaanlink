package com.samaanlink.catalogue.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.catalogue.application.CategorySummary;
import com.samaanlink.catalogue.application.CreateCategoryCommand;

import jakarta.validation.Valid;

// Explicit bean name: the legacy com.foodyexpress.controller.CategoryController (still live
// during the migration bridge) would otherwise collide on Spring's default same-simple-name
// bean naming and fail context startup with a ConflictingBeanDefinitionException.
@RestController("catalogueCategoryController")
@RequestMapping("/api/v1/categories")
public class CategoryController {

	private final CatalogueFacade catalogueFacade;

	public CategoryController(CatalogueFacade catalogueFacade) {
		this.catalogueFacade = catalogueFacade;
	}

	@PostMapping
	public ResponseEntity<CategorySummary> create(@Valid @RequestBody CreateCategoryRequest request) {
		CategorySummary category = catalogueFacade
				.createCategory(new CreateCategoryCommand(request.name(), request.parentCategoryId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(category);
	}

	@GetMapping
	public ResponseEntity<List<CategorySummary>> list() {
		return ResponseEntity.ok(catalogueFacade.listCategories());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		catalogueFacade.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}
}
