package com.samaanlink.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.samaanlink.AbstractIntegrationTest;
import com.samaanlink.billing.application.BillException;
import com.samaanlink.billing.application.BillFacade;
import com.samaanlink.billing.application.BillSummary;
import com.samaanlink.billing.application.GenerateBillCommand;
import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.catalogue.application.CategorySummary;
import com.samaanlink.catalogue.application.CreateCategoryCommand;
import com.samaanlink.catalogue.application.CreateProductCommand;
import com.samaanlink.catalogue.application.ProductSummary;
import com.samaanlink.orders.application.AddOrderLineCommand;
import com.samaanlink.orders.application.CreateOrderCommand;
import com.samaanlink.orders.application.OrderFacade;
import com.samaanlink.orders.application.OrderSummary;
import com.samaanlink.pricing.application.PricingFacade;
import com.samaanlink.pricing.application.SetPurchasePriceCommand;
import com.samaanlink.pricing.application.SetStandardSellingPriceCommand;
import com.samaanlink.restaurant.application.AddDeliveryAddressCommand;
import com.samaanlink.restaurant.application.RegisterRestaurantCommand;
import com.samaanlink.restaurant.application.RegisteredRestaurant;
import com.samaanlink.restaurant.application.RestaurantFacade;

/** Exercises the Billing -> Orders dependency: a bill can only be issued once, against a PLACED order. */
class BillingModuleIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private BillFacade billFacade;

	@Autowired
	private OrderFacade orderFacade;

	@Autowired
	private CatalogueFacade catalogueFacade;

	@Autowired
	private RestaurantFacade restaurantFacade;

	@Autowired
	private PricingFacade pricingFacade;

	@Test
	void generatesABillForAPlacedOrderAndAllowsMarkingItPaid() {
		UUID productId = createPricedProduct("SKU-BILL-1", new BigDecimal("20.00"), new BigDecimal("25.00"));
		Restaurant restaurant = createActiveRestaurant("Bill House", "owner-bill1@test.com");

		OrderSummary order = placeOrder(restaurant, productId, new BigDecimal("10"));

		BillSummary bill = billFacade.generateBill(new GenerateBillCommand(order.id()));
		assertThat(bill.status()).isEqualTo("ISSUED");
		assertThat(bill.amount()).isEqualByComparingTo(order.orderTotal());
		assertThat(bill.orderId()).isEqualTo(order.id());
		assertThat(bill.restaurantId()).isEqualTo(restaurant.restaurantId);
		assertThat(bill.paidAt()).isNull();

		BillSummary paid = billFacade.markPaid(bill.id());
		assertThat(paid.status()).isEqualTo("PAID");
		assertThat(paid.paidAt()).isNotNull();

		assertThat(billFacade.findByOrder(order.id()).id()).isEqualTo(bill.id());
		assertThat(billFacade.listByRestaurant(restaurant.restaurantId)).extracting(BillSummary::id)
				.contains(bill.id());
	}

	@Test
	void cannotBillADraftOrder() {
		Restaurant restaurant = createActiveRestaurant("Draft Bill House", "owner-bill2@test.com");
		OrderSummary draft = orderFacade
				.createOrder(new CreateOrderCommand(restaurant.restaurantId, restaurant.deliveryAddressId));

		assertThatThrownBy(() -> billFacade.generateBill(new GenerateBillCommand(draft.id())))
				.isInstanceOf(BillException.class);
	}

	@Test
	void cannotBillTheSameOrderTwiceOrPayTheSameBillTwice() {
		UUID productId = createPricedProduct("SKU-BILL-3", new BigDecimal("5.00"), new BigDecimal("8.00"));
		Restaurant restaurant = createActiveRestaurant("Double Bill House", "owner-bill3@test.com");
		OrderSummary order = placeOrder(restaurant, productId, BigDecimal.ONE);

		BillSummary bill = billFacade.generateBill(new GenerateBillCommand(order.id()));

		assertThatThrownBy(() -> billFacade.generateBill(new GenerateBillCommand(order.id())))
				.isInstanceOf(BillException.class);

		billFacade.markPaid(bill.id());
		assertThatThrownBy(() -> billFacade.markPaid(bill.id())).isInstanceOf(BillException.class);
	}

	private OrderSummary placeOrder(Restaurant restaurant, UUID productId, BigDecimal quantity) {
		OrderSummary draft = orderFacade
				.createOrder(new CreateOrderCommand(restaurant.restaurantId, restaurant.deliveryAddressId));
		orderFacade.addLine(new AddOrderLineCommand(draft.id(), productId, quantity));
		return orderFacade.placeOrder(draft.id());
	}

	private UUID createPricedProduct(String sku, BigDecimal purchasePrice, BigDecimal sellingPrice) {
		CategorySummary category = catalogueFacade.createCategory(new CreateCategoryCommand("Billing Test Category", null));
		ProductSummary product = catalogueFacade.createProduct(new CreateProductCommand("Billing Test Product", null,
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
