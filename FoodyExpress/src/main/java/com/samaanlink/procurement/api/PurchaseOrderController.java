package com.samaanlink.procurement.api;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samaanlink.procurement.application.AddPurchaseOrderLineCommand;
import com.samaanlink.procurement.application.CreatePurchaseOrderCommand;
import com.samaanlink.procurement.application.PurchaseOrderFacade;
import com.samaanlink.procurement.application.PurchaseOrderLineSummary;
import com.samaanlink.procurement.application.PurchaseOrderSummary;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

	private final PurchaseOrderFacade purchaseOrderFacade;

	public PurchaseOrderController(PurchaseOrderFacade purchaseOrderFacade) {
		this.purchaseOrderFacade = purchaseOrderFacade;
	}

	@PostMapping
	public ResponseEntity<PurchaseOrderSummary> create(@Valid @RequestBody CreatePurchaseOrderRequest request) {
		PurchaseOrderSummary po = purchaseOrderFacade.createPurchaseOrder(new CreatePurchaseOrderCommand(request.supplierId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(po);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PurchaseOrderSummary> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(purchaseOrderFacade.findPurchaseOrder(id));
	}

	@GetMapping
	public ResponseEntity<List<PurchaseOrderSummary>> list(@RequestParam(required = false) UUID supplierId) {
		List<PurchaseOrderSummary> orders = supplierId != null
				? purchaseOrderFacade.listPurchaseOrdersBySupplier(supplierId)
				: purchaseOrderFacade.listAllPurchaseOrders();
		return ResponseEntity.ok(orders);
	}

	@PostMapping("/{id}/lines")
	public ResponseEntity<PurchaseOrderLineSummary> addLine(@PathVariable UUID id,
			@Valid @RequestBody AddPurchaseOrderLineRequest request) {
		PurchaseOrderLineSummary line = purchaseOrderFacade.addLine(
				new AddPurchaseOrderLineCommand(id, request.productId(), request.quantity(), request.unitCost()));
		return ResponseEntity.status(HttpStatus.CREATED).body(line);
	}

	@DeleteMapping("/{id}/lines/{lineId}")
	public ResponseEntity<Void> removeLine(@PathVariable UUID id, @PathVariable UUID lineId) {
		purchaseOrderFacade.removeLine(id, lineId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/place")
	public ResponseEntity<PurchaseOrderSummary> place(@PathVariable UUID id) {
		return ResponseEntity.ok(purchaseOrderFacade.placeOrder(id));
	}

	@PostMapping("/{id}/receive")
	public ResponseEntity<PurchaseOrderSummary> receive(@PathVariable UUID id) {
		return ResponseEntity.ok(purchaseOrderFacade.receiveOrder(id));
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<Void> cancel(@PathVariable UUID id) {
		purchaseOrderFacade.cancelOrder(id);
		return ResponseEntity.noContent().build();
	}
}
