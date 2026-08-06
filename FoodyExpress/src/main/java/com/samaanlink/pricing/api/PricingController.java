package com.samaanlink.pricing.api;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samaanlink.pricing.application.PriceQuote;
import com.samaanlink.pricing.application.PricingFacade;
import com.samaanlink.pricing.application.QuoteLineCommand;
import com.samaanlink.pricing.application.SetProductDiscountCommand;
import com.samaanlink.pricing.application.SetProductTaxCommand;
import com.samaanlink.pricing.application.SetPurchasePriceCommand;
import com.samaanlink.pricing.application.SetRestaurantPriceCommand;
import com.samaanlink.pricing.application.SetStandardSellingPriceCommand;
import com.samaanlink.pricing.application.UpdateDeliveryFlatFeeCommand;
import com.samaanlink.pricing.application.UpdateServiceFeeRateCommand;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

	private final PricingFacade pricingFacade;

	public PricingController(PricingFacade pricingFacade) {
		this.pricingFacade = pricingFacade;
	}

	@PutMapping("/products/{productId}/purchase-price")
	public ResponseEntity<Void> setPurchasePrice(@PathVariable UUID productId,
			@Valid @RequestBody SetPriceRequest request) {
		pricingFacade.setPurchasePrice(new SetPurchasePriceCommand(productId, request.price()));
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/products/{productId}/selling-price")
	public ResponseEntity<Void> setStandardSellingPrice(@PathVariable UUID productId,
			@Valid @RequestBody SetPriceRequest request) {
		pricingFacade.setStandardSellingPrice(new SetStandardSellingPriceCommand(productId, request.price()));
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/products/{productId}/restaurant-price")
	public ResponseEntity<Void> setRestaurantPrice(@PathVariable UUID productId,
			@Valid @RequestBody SetRestaurantPriceRequest request) {
		pricingFacade.setRestaurantPrice(
				new SetRestaurantPriceCommand(productId, request.restaurantId(), request.price()));
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/products/{productId}/discount")
	public ResponseEntity<Void> setProductDiscount(@PathVariable UUID productId,
			@Valid @RequestBody SetPercentRequest request) {
		pricingFacade.setProductDiscount(new SetProductDiscountCommand(productId, request.percent()));
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/products/{productId}/tax")
	public ResponseEntity<Void> setProductTax(@PathVariable UUID productId,
			@Valid @RequestBody SetPercentRequest request) {
		pricingFacade.setProductTax(new SetProductTaxCommand(productId, request.percent()));
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/service-fee-rate")
	public ResponseEntity<Void> updateServiceFeeRate(@Valid @RequestBody SetPercentRequest request) {
		pricingFacade.updateServiceFeeRate(new UpdateServiceFeeRateCommand(request.percent()));
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/service-fee-rate")
	public ResponseEntity<BigDecimal> currentServiceFeeRate() {
		return ResponseEntity.ok(pricingFacade.currentServiceFeeRate());
	}

	@PutMapping("/delivery-flat-fee")
	public ResponseEntity<Void> updateDeliveryFlatFee(@Valid @RequestBody SetPriceRequest request) {
		pricingFacade.updateDeliveryFlatFee(new UpdateDeliveryFlatFeeCommand(request.price()));
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/delivery-flat-fee")
	public ResponseEntity<BigDecimal> currentDeliveryFlatFee() {
		return ResponseEntity.ok(pricingFacade.currentDeliveryFlatFee());
	}

	@PostMapping("/quotes")
	public ResponseEntity<PriceQuote> quoteLine(@Valid @RequestBody QuoteLineRequest request) {
		PriceQuote quote = pricingFacade
				.quoteLine(new QuoteLineCommand(request.productId(), request.restaurantId(), request.quantity()));
		return ResponseEntity.status(HttpStatus.CREATED).body(quote);
	}

	@GetMapping("/quotes/{quoteId}")
	public ResponseEntity<PriceQuote> findQuote(@PathVariable UUID quoteId) {
		return ResponseEntity.ok(pricingFacade.findQuote(quoteId));
	}

	/** Lightweight bulk price lookup for catalogue browsing - unlike /quotes, does not persist anything. */
	@PostMapping("/effective-prices")
	public ResponseEntity<Map<UUID, BigDecimal>> effectivePrices(@Valid @RequestBody EffectivePricesRequest request) {
		return ResponseEntity.ok(pricingFacade.effectiveSellingPrices(request.productIds(), request.restaurantId()));
	}
}
