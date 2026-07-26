package com.samaanlink.pricing;

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
import com.samaanlink.pricing.application.PriceQuote;
import com.samaanlink.pricing.application.PricingException;
import com.samaanlink.pricing.application.PricingFacade;
import com.samaanlink.pricing.application.QuoteLineCommand;
import com.samaanlink.pricing.application.SetProductDiscountCommand;
import com.samaanlink.pricing.application.SetProductTaxCommand;
import com.samaanlink.pricing.application.SetPurchasePriceCommand;
import com.samaanlink.pricing.application.SetRestaurantPriceCommand;
import com.samaanlink.pricing.application.SetStandardSellingPriceCommand;
import com.samaanlink.restaurant.application.RegisterRestaurantCommand;
import com.samaanlink.restaurant.application.RegisteredRestaurant;
import com.samaanlink.restaurant.application.RestaurantFacade;

/** Exercises the Pricing -> Catalogue and Pricing -> Restaurant dependencies together with the quote calculation. */
class PricingModuleIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private PricingFacade pricingFacade;

	@Autowired
	private CatalogueFacade catalogueFacade;

	@Autowired
	private RestaurantFacade restaurantFacade;

	@Test
	void seedsTheGlobalFeeRulesFromTheMigration() {
		assertThat(pricingFacade.currentServiceFeeRate()).isEqualByComparingTo("5.00");
		assertThat(pricingFacade.currentDeliveryFlatFee()).isEqualByComparingTo("1.00");
	}

	@Test
	void quotesALineApplyingDiscountTaxAndServiceFee() {
		UUID productId = createProduct("SKU-QUOTE-1");
		UUID restaurantId = createActiveRestaurant("Quote House", "owner-quote@test.com");

		pricingFacade.setPurchasePrice(new SetPurchasePriceCommand(productId, new BigDecimal("20.00")));
		pricingFacade.setStandardSellingPrice(new SetStandardSellingPriceCommand(productId, new BigDecimal("25.00")));
		pricingFacade.setProductDiscount(new SetProductDiscountCommand(productId, new BigDecimal("10")));
		pricingFacade.setProductTax(new SetProductTaxCommand(productId, new BigDecimal("15")));

		PriceQuote quote = pricingFacade.quoteLine(new QuoteLineCommand(productId, restaurantId, BigDecimal.TEN));

		assertThat(quote.unitPurchasePrice()).isEqualByComparingTo("20.00");
		assertThat(quote.unitSellingPrice()).isEqualByComparingTo("25.00");
		assertThat(quote.lineSubtotal()).isEqualByComparingTo("250.00");
		assertThat(quote.discountAmount()).isEqualByComparingTo("25.00");
		assertThat(quote.taxAmount()).isEqualByComparingTo("33.75");
		assertThat(quote.serviceFeeAmount()).isEqualByComparingTo("11.25");
		assertThat(quote.lineTotal()).isEqualByComparingTo("270.00");

		assertThat(pricingFacade.findQuote(quote.id()).lineTotal()).isEqualByComparingTo("270.00");
	}

	@Test
	void restaurantSpecificPriceOverridesTheStandardSellingPrice() {
		UUID productId = createProduct("SKU-QUOTE-2");
		UUID restaurantId = createActiveRestaurant("Override House", "owner-override@test.com");

		pricingFacade.setStandardSellingPrice(new SetStandardSellingPriceCommand(productId, new BigDecimal("25.00")));
		assertThat(pricingFacade.effectiveSellingPrice(productId, restaurantId)).isEqualByComparingTo("25.00");

		pricingFacade.setRestaurantPrice(new SetRestaurantPriceCommand(productId, restaurantId, new BigDecimal("22.00")));
		assertThat(pricingFacade.effectiveSellingPrice(productId, restaurantId)).isEqualByComparingTo("22.00");
	}

	@Test
	void quotingWithoutAPurchasePriceFails() {
		UUID productId = createProduct("SKU-QUOTE-3");
		UUID restaurantId = createActiveRestaurant("No Purchase Price House", "owner-nopp@test.com");
		pricingFacade.setStandardSellingPrice(new SetStandardSellingPriceCommand(productId, new BigDecimal("10.00")));

		assertThatThrownBy(() -> pricingFacade.quoteLine(new QuoteLineCommand(productId, restaurantId, BigDecimal.ONE)))
				.isInstanceOf(PricingException.class);
	}

	private UUID createProduct(String sku) {
		CategorySummary category = catalogueFacade.createCategory(new CreateCategoryCommand("Pricing Test Category", null));
		ProductSummary product = catalogueFacade.createProduct(new CreateProductCommand("Pricing Test Product", null,
				category.id(), sku, null, "KG", "KG", BigDecimal.ONE, BigDecimal.ONE, null));
		return product.id();
	}

	private UUID createActiveRestaurant(String name, String ownerEmail) {
		RegisteredRestaurant result = restaurantFacade.registerRestaurant(new RegisterRestaurantCommand(name,
				new BigDecimal("500.00"), 14, "Main Branch", "Mogadishu", ownerEmail, "SuperSecret1", "Amina",
				"Yusuf", "+252700000000"));
		restaurantFacade.activateRestaurant(result.restaurant().id());
		return result.restaurant().id();
	}
}
