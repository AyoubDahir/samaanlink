package com.samaanlink.catalogue.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samaanlink.catalogue.application.AddProductImageCommand;
import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.catalogue.application.CreateProductCommand;
import com.samaanlink.catalogue.application.ProductSummary;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private final CatalogueFacade catalogueFacade;

	public ProductController(CatalogueFacade catalogueFacade) {
		this.catalogueFacade = catalogueFacade;
	}

	@PostMapping
	public ResponseEntity<ProductSummary> create(@Valid @RequestBody CreateProductRequest request) {
		ProductSummary product = catalogueFacade.createProduct(new CreateProductCommand(request.name(),
				request.description(), request.categoryId(), request.sku(), request.barcode(),
				request.purchaseUnitCode(), request.sellingUnitCode(), request.packageSize(),
				request.unitsPerPackage(), request.weightKg()));
		return ResponseEntity.status(HttpStatus.CREATED).body(product);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductSummary> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(catalogueFacade.findProduct(id));
	}

	@GetMapping
	public ResponseEntity<List<ProductSummary>> list(@RequestParam(required = false) UUID categoryId) {
		List<ProductSummary> products = categoryId != null ? catalogueFacade.listProductsByCategory(categoryId)
				: catalogueFacade.listProducts();
		return ResponseEntity.ok(products);
	}

	@PostMapping("/{id}/images")
	public ResponseEntity<Void> addImage(@PathVariable UUID id, @Valid @RequestBody AddProductImageRequest request) {
		catalogueFacade.addProductImage(new AddProductImageCommand(id, request.url(), request.sortOrder()));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/{id}/activate")
	public ResponseEntity<Void> activate(@PathVariable UUID id) {
		catalogueFacade.activateProduct(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/discontinue")
	public ResponseEntity<Void> discontinue(@PathVariable UUID id) {
		catalogueFacade.discontinueProduct(id);
		return ResponseEntity.noContent().build();
	}
}
