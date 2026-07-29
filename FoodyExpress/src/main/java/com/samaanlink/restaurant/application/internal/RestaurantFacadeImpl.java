package com.samaanlink.restaurant.application.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samaanlink.identity.application.IdentityFacade;
import com.samaanlink.identity.application.RegisterUserCommand;
import com.samaanlink.identity.application.UserSummary;
import com.samaanlink.restaurant.application.AddBranchCommand;
import com.samaanlink.restaurant.application.AddDeliveryAddressCommand;
import com.samaanlink.restaurant.application.AddRestaurantContactCommand;
import com.samaanlink.restaurant.application.BranchSummary;
import com.samaanlink.restaurant.application.DeliveryAddressSummary;
import com.samaanlink.restaurant.application.RegisterRestaurantCommand;
import com.samaanlink.restaurant.application.RegisteredRestaurant;
import com.samaanlink.restaurant.application.RestaurantException;
import com.samaanlink.restaurant.application.RestaurantFacade;
import com.samaanlink.restaurant.application.RestaurantSummary;
import com.samaanlink.restaurant.application.UpdateCreditLimitCommand;
import com.samaanlink.restaurant.domain.DeliveryAddress;
import com.samaanlink.restaurant.domain.Restaurant;
import com.samaanlink.restaurant.domain.RestaurantBranch;
import com.samaanlink.restaurant.domain.RestaurantContact;
import com.samaanlink.restaurant.domain.RestaurantUser;
import com.samaanlink.restaurant.infrastructure.DeliveryAddressRepository;
import com.samaanlink.restaurant.infrastructure.RestaurantBranchRepository;
import com.samaanlink.restaurant.infrastructure.RestaurantContactRepository;
import com.samaanlink.restaurant.infrastructure.RestaurantRepository;
import com.samaanlink.restaurant.infrastructure.RestaurantUserRepository;

@Service
public class RestaurantFacadeImpl implements RestaurantFacade {

	private final RestaurantRepository restaurantRepository;
	private final RestaurantBranchRepository branchRepository;
	private final DeliveryAddressRepository deliveryAddressRepository;
	private final RestaurantContactRepository contactRepository;
	private final RestaurantUserRepository restaurantUserRepository;
	private final IdentityFacade identityFacade;

	public RestaurantFacadeImpl(RestaurantRepository restaurantRepository, RestaurantBranchRepository branchRepository,
			DeliveryAddressRepository deliveryAddressRepository, RestaurantContactRepository contactRepository,
			RestaurantUserRepository restaurantUserRepository, IdentityFacade identityFacade) {
		this.restaurantRepository = restaurantRepository;
		this.branchRepository = branchRepository;
		this.deliveryAddressRepository = deliveryAddressRepository;
		this.contactRepository = contactRepository;
		this.restaurantUserRepository = restaurantUserRepository;
		this.identityFacade = identityFacade;
	}

	@Override
	@Transactional
	public RegisteredRestaurant registerRestaurant(RegisterRestaurantCommand command) {
		Restaurant restaurant = restaurantRepository
				.save(Restaurant.create(command.restaurantName(), command.creditLimit(), command.paymentTermDays()));

		RestaurantBranch branch = branchRepository.save(RestaurantBranch.create(restaurant,
				command.primaryBranchName(), command.primaryBranchCity(), true));

		UserSummary owner = identityFacade.registerUser(new RegisterUserCommand(command.ownerEmail(),
				command.ownerPassword(), command.ownerFirstName(), command.ownerLastName(), command.ownerPhone(),
				"RESTAURANT_OWNER"));

		restaurantUserRepository.save(RestaurantUser.link(restaurant, owner.userId()));

		return new RegisteredRestaurant(toSummary(restaurant), owner.userId(), branch.getId());
	}

	@Override
	@Transactional
	public BranchSummary addBranch(AddBranchCommand command) {
		Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
				.orElseThrow(() -> new RestaurantException("Restaurant not found"));
		RestaurantBranch branch = branchRepository
				.save(RestaurantBranch.create(restaurant, command.name(), command.city(), command.primary()));
		return toSummary(branch);
	}

	@Override
	@Transactional
	public DeliveryAddressSummary addDeliveryAddress(AddDeliveryAddressCommand command) {
		RestaurantBranch branch = branchRepository.findById(command.branchId())
				.orElseThrow(() -> new RestaurantException("Branch not found"));
		DeliveryAddress address = deliveryAddressRepository.save(DeliveryAddress.create(branch, command.label(),
				command.addressLine(), command.city(), command.defaultAddress()));
		return toSummary(address);
	}

	@Override
	@Transactional
	public void addContact(AddRestaurantContactCommand command) {
		Restaurant restaurant = restaurantRepository.findById(command.restaurantId())
				.orElseThrow(() -> new RestaurantException("Restaurant not found"));
		contactRepository.save(RestaurantContact.create(restaurant, command.name(), command.phone(), command.email(),
				command.roleTitle()));
	}

	@Override
	@Transactional
	public void updateCreditLimit(UpdateCreditLimitCommand command) {
		restaurantRepository.findById(command.restaurantId())
				.orElseThrow(() -> new RestaurantException("Restaurant not found"))
				.updateCreditLimit(command.newLimit());
	}

	@Override
	@Transactional
	public void activateRestaurant(UUID restaurantId) {
		restaurantRepository.findById(restaurantId).orElseThrow(() -> new RestaurantException("Restaurant not found"))
				.activate();
	}

	@Override
	@Transactional
	public void suspendRestaurant(UUID restaurantId) {
		restaurantRepository.findById(restaurantId).orElseThrow(() -> new RestaurantException("Restaurant not found"))
				.suspend();
	}

	@Override
	@Transactional
	public void closeRestaurant(UUID restaurantId) {
		restaurantRepository.findById(restaurantId).orElseThrow(() -> new RestaurantException("Restaurant not found"))
				.close();
	}

	@Override
	@Transactional(readOnly = true)
	public RestaurantSummary findRestaurant(UUID restaurantId) {
		return restaurantRepository.findById(restaurantId).map(this::toSummary)
				.orElseThrow(() -> new RestaurantException("Restaurant not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public void validateActiveRestaurant(UUID restaurantId) {
		Restaurant restaurant = restaurantRepository.findById(restaurantId)
				.orElseThrow(() -> new RestaurantException("Restaurant not found"));
		if (!restaurant.isActive()) {
			throw new RestaurantException("Restaurant is not active");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<RestaurantSummary> listRestaurants() {
		return restaurantRepository.findAll().stream().map(this::toSummary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public RestaurantSummary findRestaurantForUser(UUID userId) {
		return restaurantUserRepository.findByUserId(userId).map(RestaurantUser::getRestaurant).map(this::toSummary)
				.orElseThrow(() -> new RestaurantException("No restaurant linked to this user"));
	}

	@Override
	@Transactional(readOnly = true)
	public List<BranchSummary> listBranches(UUID restaurantId) {
		return branchRepository.findByRestaurantId(restaurantId).stream().map(this::toSummary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<DeliveryAddressSummary> listDeliveryAddresses(UUID branchId) {
		return deliveryAddressRepository.findByBranchId(branchId).stream().map(this::toSummary).toList();
	}

	private RestaurantSummary toSummary(Restaurant restaurant) {
		return new RestaurantSummary(restaurant.getId(), restaurant.getName(), restaurant.getCreditLimit(),
				restaurant.getPaymentTermDays(), restaurant.getStatus().name());
	}

	private BranchSummary toSummary(RestaurantBranch branch) {
		return new BranchSummary(branch.getId(), branch.getRestaurant().getId(), branch.getName(), branch.getCity(),
				branch.isPrimary());
	}

	private DeliveryAddressSummary toSummary(DeliveryAddress address) {
		return new DeliveryAddressSummary(address.getId(), address.getBranch().getId(), address.getLabel(),
				address.getAddressLine(), address.getCity(), address.isDefaultAddress());
	}
}
