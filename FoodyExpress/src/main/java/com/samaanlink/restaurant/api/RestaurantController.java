package com.samaanlink.restaurant.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samaanlink.restaurant.application.AddBranchCommand;
import com.samaanlink.restaurant.application.AddDeliveryAddressCommand;
import com.samaanlink.restaurant.application.AddRestaurantContactCommand;
import com.samaanlink.restaurant.application.BranchSummary;
import com.samaanlink.restaurant.application.DeliveryAddressSummary;
import com.samaanlink.restaurant.application.RegisterRestaurantCommand;
import com.samaanlink.restaurant.application.RegisteredRestaurant;
import com.samaanlink.restaurant.application.RestaurantFacade;
import com.samaanlink.restaurant.application.RestaurantSummary;
import com.samaanlink.restaurant.application.UpdateCreditLimitCommand;

import jakarta.validation.Valid;

// Explicit bean name: the legacy com.foodyexpress.controller.RestaurantController (still live
// during the migration bridge) would otherwise collide on Spring's default same-simple-name
// bean naming and fail context startup with a ConflictingBeanDefinitionException.
@RestController("restaurantRestaurantController")
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

	private final RestaurantFacade restaurantFacade;

	public RestaurantController(RestaurantFacade restaurantFacade) {
		this.restaurantFacade = restaurantFacade;
	}

	@PostMapping
	public ResponseEntity<RegisteredRestaurant> register(@Valid @RequestBody RegisterRestaurantRequest request) {
		RegisteredRestaurant result = restaurantFacade.registerRestaurant(new RegisterRestaurantCommand(
				request.restaurantName(), request.creditLimit(), request.paymentTermDays(),
				request.primaryBranchName(), request.primaryBranchCity(), request.ownerEmail(),
				request.ownerPassword(), request.ownerFirstName(), request.ownerLastName(), request.ownerPhone()));
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<RestaurantSummary> findById(@PathVariable UUID id) {
		return ResponseEntity.ok(restaurantFacade.findRestaurant(id));
	}

	@PatchMapping("/{id}/credit-limit")
	public ResponseEntity<Void> updateCreditLimit(@PathVariable UUID id,
			@Valid @RequestBody UpdateCreditLimitRequest request) {
		restaurantFacade.updateCreditLimit(new UpdateCreditLimitCommand(id, request.newLimit()));
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/activate")
	public ResponseEntity<Void> activate(@PathVariable UUID id) {
		restaurantFacade.activateRestaurant(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/suspend")
	public ResponseEntity<Void> suspend(@PathVariable UUID id) {
		restaurantFacade.suspendRestaurant(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/close")
	public ResponseEntity<Void> close(@PathVariable UUID id) {
		restaurantFacade.closeRestaurant(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/contacts")
	public ResponseEntity<Void> addContact(@PathVariable UUID id, @Valid @RequestBody AddContactRequest request) {
		restaurantFacade.addContact(new AddRestaurantContactCommand(id, request.name(), request.phone(),
				request.email(), request.roleTitle()));
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/{id}/branches")
	public ResponseEntity<BranchSummary> addBranch(@PathVariable UUID id, @Valid @RequestBody AddBranchRequest request) {
		BranchSummary branch = restaurantFacade
				.addBranch(new AddBranchCommand(id, request.name(), request.city(), request.primary()));
		return ResponseEntity.status(HttpStatus.CREATED).body(branch);
	}

	@GetMapping("/{id}/branches")
	public ResponseEntity<List<BranchSummary>> listBranches(@PathVariable UUID id) {
		return ResponseEntity.ok(restaurantFacade.listBranches(id));
	}

	@PostMapping("/branches/{branchId}/addresses")
	public ResponseEntity<DeliveryAddressSummary> addDeliveryAddress(@PathVariable UUID branchId,
			@Valid @RequestBody AddDeliveryAddressRequest request) {
		DeliveryAddressSummary address = restaurantFacade.addDeliveryAddress(new AddDeliveryAddressCommand(branchId,
				request.label(), request.addressLine(), request.city(), request.defaultAddress()));
		return ResponseEntity.status(HttpStatus.CREATED).body(address);
	}

	@GetMapping("/branches/{branchId}/addresses")
	public ResponseEntity<List<DeliveryAddressSummary>> listDeliveryAddresses(@PathVariable UUID branchId) {
		return ResponseEntity.ok(restaurantFacade.listDeliveryAddresses(branchId));
	}
}
