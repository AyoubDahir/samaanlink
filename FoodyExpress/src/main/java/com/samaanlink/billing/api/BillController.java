package com.samaanlink.billing.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samaanlink.billing.application.BillFacade;
import com.samaanlink.billing.application.BillSummary;
import com.samaanlink.billing.application.GenerateBillCommand;

import jakarta.validation.Valid;

@RestController("billingBillController")
@RequestMapping("/api/v1/bills")
public class BillController {

	private final BillFacade billFacade;

	public BillController(BillFacade billFacade) {
		this.billFacade = billFacade;
	}

	@PostMapping
	public ResponseEntity<BillSummary> generate(@Valid @RequestBody GenerateBillRequest request) {
		BillSummary bill = billFacade.generateBill(new GenerateBillCommand(request.orderId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(bill);
	}

	@GetMapping("/{id}")
	public ResponseEntity<BillSummary> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(billFacade.findBill(id));
	}

	@GetMapping("/by-order/{orderId}")
	public ResponseEntity<BillSummary> findByOrder(@PathVariable UUID orderId) {
		return ResponseEntity.ok(billFacade.findByOrder(orderId));
	}

	@GetMapping
	public ResponseEntity<List<BillSummary>> listByRestaurant(@RequestParam UUID restaurantId) {
		return ResponseEntity.ok(billFacade.listByRestaurant(restaurantId));
	}

	@PostMapping("/{id}/pay")
	public ResponseEntity<BillSummary> pay(@PathVariable UUID id) {
		return ResponseEntity.ok(billFacade.markPaid(id));
	}
}
