package com.samaanlink.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.samaanlink.AbstractIntegrationTest;
import com.samaanlink.identity.application.IdentityFacade;
import com.samaanlink.identity.application.LoginCommand;
import com.samaanlink.restaurant.application.AddBranchCommand;
import com.samaanlink.restaurant.application.BranchSummary;
import com.samaanlink.restaurant.application.RegisterRestaurantCommand;
import com.samaanlink.restaurant.application.RegisteredRestaurant;
import com.samaanlink.restaurant.application.RestaurantException;
import com.samaanlink.restaurant.application.RestaurantFacade;

/** Exercises the Restaurant -> Identity dependency: registering a restaurant creates a real, loginable owner account. */
class RestaurantModuleIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private RestaurantFacade restaurantFacade;

	@Autowired
	private IdentityFacade identityFacade;

	@Test
	void registeringARestaurantCreatesAWorkingOwnerLogin() {
		RegisteredRestaurant result = restaurantFacade.registerRestaurant(new RegisterRestaurantCommand(
				"Nomad Diner", new BigDecimal("500.00"), 14, "Main Branch", "Mogadishu", "owner@nomaddiner.test",
				"SuperSecret1", "Amina", "Yusuf", "+252700000000"));

		assertThat(result.restaurant().status()).isEqualTo("PENDING_APPROVAL");

		var auth = identityFacade.login(new LoginCommand("owner@nomaddiner.test", "SuperSecret1"));
		assertThat(auth.roleName()).isEqualTo("RESTAURANT_OWNER");
		assertThat(auth.userId()).isEqualTo(result.ownerUserId());

		BranchSummary addedBranch = restaurantFacade
				.addBranch(new AddBranchCommand(result.restaurant().id(), "Second Branch", "Hargeisa", false));
		assertThat(restaurantFacade.listBranches(result.restaurant().id()))
				.extracting(BranchSummary::id)
				.contains(result.primaryBranchId(), addedBranch.id());
	}

	@Test
	void validateActiveRestaurantRejectsAPendingRestaurant() {
		RegisteredRestaurant result = restaurantFacade.registerRestaurant(new RegisterRestaurantCommand(
				"Pending Place", BigDecimal.ZERO, 0, "HQ", "Kismayo", "owner2@pending.test", "SuperSecret1", "Ali",
				"Noor", null));

		assertThatThrownBy(() -> restaurantFacade.validateActiveRestaurant(result.restaurant().id()))
				.isInstanceOf(RestaurantException.class);

		restaurantFacade.activateRestaurant(result.restaurant().id());
		restaurantFacade.validateActiveRestaurant(result.restaurant().id());
	}
}
