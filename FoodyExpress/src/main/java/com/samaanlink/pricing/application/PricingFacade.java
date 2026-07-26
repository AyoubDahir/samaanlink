package com.samaanlink.pricing.application;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The only way another module may interact with Pricing. Orders calls {@link #quoteLine} to obtain
 * an immutable, persisted price quote for a line before confirming an order.
 */
public interface PricingFacade {

	void setPurchasePrice(SetPurchasePriceCommand command);

	void setStandardSellingPrice(SetStandardSellingPriceCommand command);

	void setRestaurantPrice(SetRestaurantPriceCommand command);

	void setProductDiscount(SetProductDiscountCommand command);

	void setProductTax(SetProductTaxCommand command);

	void updateServiceFeeRate(UpdateServiceFeeRateCommand command);

	void updateDeliveryFlatFee(UpdateDeliveryFlatFeeCommand command);

	BigDecimal currentServiceFeeRate();

	BigDecimal currentDeliveryFlatFee();

	/** Resolves the effective selling price for a product/restaurant pair: the restaurant-specific override if one exists, otherwise the standard selling price. */
	BigDecimal effectiveSellingPrice(UUID productId, UUID restaurantId);

	/** Calculates and persists an immutable {@link PriceQuote} for a line; Orders re-fetches it by id rather than recomputing. */
	PriceQuote quoteLine(QuoteLineCommand command);

	PriceQuote findQuote(UUID quoteId);
}
