package com.samaanlink.supplier.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samaanlink.supplier.application.AddSupplierContactCommand;
import com.samaanlink.supplier.application.LinkProductCommand;
import com.samaanlink.supplier.application.RegisterSupplierCommand;
import com.samaanlink.supplier.application.SupplierFacade;
import com.samaanlink.supplier.application.SupplierSummary;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

	private final SupplierFacade supplierFacade;

	public SupplierController(SupplierFacade supplierFacade) {
		this.supplierFacade = supplierFacade;
	}

	@PostMapping
	public ResponseEntity<SupplierSummary> register(@Valid @RequestBody RegisterSupplierRequest request) {
		SupplierSummary supplier = supplierFacade.registerSupplier(
				new RegisterSupplierCommand(request.name(), request.leadTimeDays(), request.paymentTermDays()));
		return ResponseEntity.status(HttpStatus.CREATED).body(supplier);
	}

	@GetMapping("/{id}")
	public ResponseEntity<SupplierSummary> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(supplierFacade.findSupplier(id));
	}

	@PostMapping("/{id}/contacts")
	public ResponseEntity<Void> addContact(@PathVariable UUID id, @Valid @RequestBody AddContactRequest request) {
		supplierFacade.addContact(new AddSupplierContactCommand(id, request.name(), request.phone(),
				request.email(), request.roleTitle()));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/{id}/products")
	public ResponseEntity<Void> linkProduct(@PathVariable UUID id, @Valid @RequestBody LinkProductRequest request) {
		supplierFacade.linkProduct(new LinkProductCommand(id, request.productId(), request.supplierSku()));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PutMapping("/{id}/activate")
	public ResponseEntity<Void> activate(@PathVariable UUID id) {
		supplierFacade.activateSupplier(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/suspend")
	public ResponseEntity<Void> suspend(@PathVariable UUID id) {
		supplierFacade.suspendSupplier(id);
		return ResponseEntity.noContent().build();
	}
}
