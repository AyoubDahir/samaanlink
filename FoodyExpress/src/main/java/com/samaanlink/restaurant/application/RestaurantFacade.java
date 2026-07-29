package com.samaanlink.restaurant.application;

import java.util.List;
import java.util.UUID;

public interface RestaurantFacade {

	RegisteredRestaurant registerRestaurant(RegisterRestaurantCommand command);

	BranchSummary addBranch(AddBranchCommand command);

	DeliveryAddressSummary addDeliveryAddress(AddDeliveryAddressCommand command);

	void addContact(AddRestaurantContactCommand command);

	void updateCreditLimit(UpdateCreditLimitCommand command);

	void activateRestaurant(UUID restaurantId);

	void suspendRestaurant(UUID restaurantId);

	void closeRestaurant(UUID restaurantId);

	RestaurantSummary findRestaurant(UUID restaurantId);

	List<RestaurantSummary> listRestaurants();

	/** Resolves the restaurant a given Identity-module user belongs to (owner or staff). */
	RestaurantSummary findRestaurantForUser(UUID userId);

	/** Throws {@link RestaurantException} unless the restaurant exists and is ACTIVE - used by Pricing/Orders before confirming an order. */
	void validateActiveRestaurant(UUID restaurantId);

	List<BranchSummary> listBranches(UUID restaurantId);

	List<DeliveryAddressSummary> listDeliveryAddresses(UUID branchId);
}
