package com.samaanlink.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.samaanlink.AbstractIntegrationTest;
import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.catalogue.application.CategorySummary;
import com.samaanlink.catalogue.application.CreateCategoryCommand;
import com.samaanlink.catalogue.application.CreateProductCommand;
import com.samaanlink.catalogue.application.ProductSummary;
import com.samaanlink.orders.application.AddOrderLineCommand;
import com.samaanlink.orders.application.CreateOrderCommand;
import com.samaanlink.orders.application.OrderException;
import com.samaanlink.orders.application.OrderFacade;
import com.samaanlink.orders.application.OrderLineSummary;
import com.samaanlink.orders.application.OrderSummary;
import com.samaanlink.pricing.application.PricingFacade;
import com.samaanlink.pricing.application.SetPurchasePriceCommand;
import com.samaanlink.pricing.application.SetStandardSellingPriceCommand;
import com.samaanlink.restaurant.application.AddDeliveryAddressCommand;
import com.samaanlink.restaurant.application.RegisterRestaurantCommand;
import com.samaanlink.restaurant.application.RegisteredRestaurant;
import com.samaanlink.restaurant.application.RestaurantFacade;

/** Exercises the Orders -> Catalogue/Restaurant/Pricing dependencies through a full order lifecycle. */
class OrdersModuleIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private OrderFacade orderFacade;

	@Autowired
	private CatalogueFacade catalogueFacade;

	@Autowired
	private RestaurantFacade restaurantFacade;

	@Autowired
	private PricingFacade pricingFacade;

	@Test
	void createsAnOrderAddsLinesAndPlacesIt() {
		UUID productId = createPricedProduct("SKU-ORDER-1", new BigDecimal("20.00"), new BigDecimal("25.00"));
		Restaurant restaurant = createActiveRestaurant("Order House", "owner-order1@test.com");

		OrderSummary draft = orderFacade
				.createOrder(new CreateOrderCommand(restaurant.restaurantId, restaurant.deliveryAddressId));
		assertThat(draft.status()).isEqualTo("DRAFT");

		// unit selling price 25.00 x qty 10 = 250.00 subtotal, no discount/tax, + 5% default service fee = 262.50
		OrderLineSummary line = orderFacade
				.addLine(new AddOrderLineCommand(draft.id(), productId, new BigDecimal("10")));
		assertThat(line.lineTotal()).isEqualByComparingTo("262.50");

		OrderSummary placed = orderFacade.placeOrder(draft.id());
		assertThat(placed.status()).isEqualTo("PLACED");
		assertThat(placed.subtotal()).isEqualByComparingTo("262.50");
		assertThat(placed.deliveryFee()).isEqualByComparingTo("1.00");
		assertThat(placed.orderTotal()).isEqualByComparingTo("263.50");
		assertThat(placed.lines()).hasSize(1);

		assertThat(orderFacade.findOrder(draft.id()).orderTotal()).isEqualByComparingTo("263.50");
		assertThat(orderFacade.listOrdersByRestaurant(restaurant.restaurantId)).extracting(OrderSummary::id)
				.contains(draft.id());
	}

	@Test
	void cannotPlaceAnOrderWithNoLines() {
		Restaurant restaurant = createActiveRestaurant("Empty Order House", "owner-order2@test.com");
		OrderSummary draft = orderFacade
				.createOrder(new CreateOrderCommand(restaurant.restaurantId, restaurant.deliveryAddressId));

		assertThatThrownBy(() -> orderFacade.placeOrder(draft.id())).isInstanceOf(OrderException.class);
	}

	@Test
	void cannotModifyOrPlaceAnAlreadyPlacedOrderAgain() {
		UUID productId = createPricedProduct("SKU-ORDER-3", new BigDecimal("5.00"), new BigDecimal("8.00"));
		Restaurant restaurant = createActiveRestaurant("Locked Order House", "owner-order3@test.com");

		OrderSummary draft = orderFacade
				.createOrder(new CreateOrderCommand(restaurant.restaurantId, restaurant.deliveryAddressId));
		orderFacade.addLine(new AddOrderLineCommand(draft.id(), productId, BigDecimal.ONE));
		orderFacade.placeOrder(draft.id());

		assertThatThrownBy(() -> orderFacade.addLine(new AddOrderLineCommand(draft.id(), productId, BigDecimal.ONE)))
				.isInstanceOf(OrderException.class);
		assertThatThrownBy(() -> orderFacade.placeOrder(draft.id())).isInstanceOf(OrderException.class);
	}

	@Test
	void cancelsADraftOrderAndDeliversAPlacedOrder() {
		UUID productId = createPricedProduct("SKU-ORDER-4", new BigDecimal("5.00"), new BigDecimal("8.00"));
		Restaurant restaurant = createActiveRestaurant("Lifecycle House", "owner-order4@test.com");

		OrderSummary draft = orderFacade
				.createOrder(new CreateOrderCommand(restaurant.restaurantId, restaurant.deliveryAddressId));
		orderFacade.cancelOrder(draft.id());
		assertThat(orderFacade.findOrder(draft.id()).status()).isEqualTo("CANCELLED");

		OrderSummary toDeliver = orderFacade
				.createOrder(new CreateOrderCommand(restaurant.restaurantId, restaurant.deliveryAddressId));
		orderFacade.addLine(new AddOrderLineCommand(toDeliver.id(), productId, BigDecimal.ONE));
		orderFacade.placeOrder(toDeliver.id());
		orderFacade.markDelivered(toDeliver.id());
		assertThat(orderFacade.findOrder(toDeliver.id()).status()).isEqualTo("DELIVERED");

		assertThatThrownBy(() -> orderFacade.markDelivered(toDeliver.id())).isInstanceOf(OrderException.class);
	}

	private UUID createPricedProduct(String sku, BigDecimal purchasePrice, BigDecimal sellingPrice) {
		CategorySummary category = catalogueFacade.createCategory(new CreateCategoryCommand("Orders Test Category", null));
		ProductSummary product = catalogueFacade.createProduct(new CreateProductCommand("Orders Test Product", null,
				category.id(), sku, null, "KG", "KG", BigDecimal.ONE, BigDecimal.ONE, null));
		pricingFacade.setPurchasePrice(new SetPurchasePriceCommand(product.id(), purchasePrice));
		pricingFacade.setStandardSellingPrice(new SetStandardSellingPriceCommand(product.id(), sellingPrice));
		return product.id();
	}

	private Restaurant createActiveRestaurant(String name, String ownerEmail) {
		RegisteredRestaurant result = restaurantFacade.registerRestaurant(new RegisterRestaurantCommand(name,
				new BigDecimal("500.00"), 14, "Main Branch", "Mogadishu", ownerEmail, "SuperSecret1", "Amina",
				"Yusuf", "+252700000000"));
		restaurantFacade.activateRestaurant(result.restaurant().id());
		UUID addressId = restaurantFacade
				.addDeliveryAddress(new AddDeliveryAddressCommand(result.primaryBranchId(), "HQ", "Main St 1",
						"Mogadishu", true))
				.id();
		return new Restaurant(result.restaurant().id(), addressId);
	}

	private record Restaurant(UUID restaurantId, UUID deliveryAddressId) {
	}
}
