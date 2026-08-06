package com.samaanlink.pricing.application.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.pricing.application.PriceQuote;
import com.samaanlink.pricing.application.PricingException;
import com.samaanlink.pricing.application.PricingFacade;
import com.samaanlink.pricing.application.QuoteLineCommand;
import com.samaanlink.pricing.application.SetProductDiscountCommand;
import com.samaanlink.pricing.application.SetProductTaxCommand;
import com.samaanlink.pricing.application.SetPurchasePriceCommand;
import com.samaanlink.pricing.application.SetRestaurantPriceCommand;
import com.samaanlink.pricing.application.SetStandardSellingPriceCommand;
import com.samaanlink.pricing.application.UpdateDeliveryFlatFeeCommand;
import com.samaanlink.pricing.application.UpdateServiceFeeRateCommand;
import com.samaanlink.pricing.domain.DeliveryFeeRule;
import com.samaanlink.pricing.domain.PriceSnapshot;
import com.samaanlink.pricing.domain.ProductDiscount;
import com.samaanlink.pricing.domain.ProductTax;
import com.samaanlink.pricing.domain.PurchasePrice;
import com.samaanlink.pricing.domain.RestaurantPrice;
import com.samaanlink.pricing.domain.ServiceFeeRule;
import com.samaanlink.pricing.domain.StandardSellingPrice;
import com.samaanlink.pricing.infrastructure.DeliveryFeeRuleRepository;
import com.samaanlink.pricing.infrastructure.PriceSnapshotRepository;
import com.samaanlink.pricing.infrastructure.ProductDiscountRepository;
import com.samaanlink.pricing.infrastructure.ProductTaxRepository;
import com.samaanlink.pricing.infrastructure.PurchasePriceRepository;
import com.samaanlink.pricing.infrastructure.RestaurantPriceRepository;
import com.samaanlink.pricing.infrastructure.ServiceFeeRuleRepository;
import com.samaanlink.pricing.infrastructure.StandardSellingPriceRepository;
import com.samaanlink.restaurant.application.RestaurantFacade;

@Service
public class PricingFacadeImpl implements PricingFacade {

	private static final int MONEY_SCALE = 2;

	private final PurchasePriceRepository purchasePriceRepository;
	private final StandardSellingPriceRepository standardSellingPriceRepository;
	private final RestaurantPriceRepository restaurantPriceRepository;
	private final ProductDiscountRepository productDiscountRepository;
	private final ProductTaxRepository productTaxRepository;
	private final ServiceFeeRuleRepository serviceFeeRuleRepository;
	private final DeliveryFeeRuleRepository deliveryFeeRuleRepository;
	private final PriceSnapshotRepository priceSnapshotRepository;
	private final CatalogueFacade catalogueFacade;
	private final RestaurantFacade restaurantFacade;

	public PricingFacadeImpl(PurchasePriceRepository purchasePriceRepository,
			StandardSellingPriceRepository standardSellingPriceRepository,
			RestaurantPriceRepository restaurantPriceRepository, ProductDiscountRepository productDiscountRepository,
			ProductTaxRepository productTaxRepository, ServiceFeeRuleRepository serviceFeeRuleRepository,
			DeliveryFeeRuleRepository deliveryFeeRuleRepository, PriceSnapshotRepository priceSnapshotRepository,
			CatalogueFacade catalogueFacade, RestaurantFacade restaurantFacade) {
		this.purchasePriceRepository = purchasePriceRepository;
		this.standardSellingPriceRepository = standardSellingPriceRepository;
		this.restaurantPriceRepository = restaurantPriceRepository;
		this.productDiscountRepository = productDiscountRepository;
		this.productTaxRepository = productTaxRepository;
		this.serviceFeeRuleRepository = serviceFeeRuleRepository;
		this.deliveryFeeRuleRepository = deliveryFeeRuleRepository;
		this.priceSnapshotRepository = priceSnapshotRepository;
		this.catalogueFacade = catalogueFacade;
		this.restaurantFacade = restaurantFacade;
	}

	@Override
	@Transactional
	public void setPurchasePrice(SetPurchasePriceCommand command) {
		requireProduct(command.productId());
		purchasePriceRepository.findById(command.productId())
				.ifPresentOrElse(existing -> existing.updatePrice(command.price()), () -> purchasePriceRepository
						.save(PurchasePrice.of(command.productId(), command.price())));
	}

	@Override
	@Transactional
	public void setStandardSellingPrice(SetStandardSellingPriceCommand command) {
		requireProduct(command.productId());
		standardSellingPriceRepository.findById(command.productId())
				.ifPresentOrElse(existing -> existing.updatePrice(command.price()), () -> standardSellingPriceRepository
						.save(StandardSellingPrice.of(command.productId(), command.price())));
	}

	@Override
	@Transactional
	public void setRestaurantPrice(SetRestaurantPriceCommand command) {
		requireProduct(command.productId());
		restaurantFacade.validateActiveRestaurant(command.restaurantId());
		restaurantPriceRepository.findByProductIdAndRestaurantId(command.productId(), command.restaurantId())
				.ifPresentOrElse(existing -> existing.updatePrice(command.price()),
						() -> restaurantPriceRepository.save(RestaurantPrice.of(command.productId(),
								command.restaurantId(), command.price())));
	}

	@Override
	@Transactional
	public void setProductDiscount(SetProductDiscountCommand command) {
		requireProduct(command.productId());
		productDiscountRepository.findById(command.productId())
				.ifPresentOrElse(existing -> existing.updateDiscountPercent(command.discountPercent()),
						() -> productDiscountRepository
								.save(ProductDiscount.of(command.productId(), command.discountPercent())));
	}

	@Override
	@Transactional
	public void setProductTax(SetProductTaxCommand command) {
		requireProduct(command.productId());
		productTaxRepository.findById(command.productId())
				.ifPresentOrElse(existing -> existing.updateTaxPercent(command.taxPercent()),
						() -> productTaxRepository.save(ProductTax.of(command.productId(), command.taxPercent())));
	}

	@Override
	@Transactional
	public void updateServiceFeeRate(UpdateServiceFeeRateCommand command) {
		currentServiceFeeRuleEntity().updateRatePercent(command.ratePercent());
	}

	@Override
	@Transactional
	public void updateDeliveryFlatFee(UpdateDeliveryFlatFeeCommand command) {
		currentDeliveryFeeRuleEntity().updateFlatFee(command.flatFee());
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal currentServiceFeeRate() {
		return currentServiceFeeRuleEntity().getRatePercent();
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal currentDeliveryFlatFee() {
		return currentDeliveryFeeRuleEntity().getFlatFee();
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal effectiveSellingPrice(UUID productId, UUID restaurantId) {
		return restaurantPriceRepository.findByProductIdAndRestaurantId(productId, restaurantId)
				.map(RestaurantPrice::getPrice)
				.orElseGet(() -> standardSellingPriceRepository.findById(productId)
						.map(StandardSellingPrice::getPrice)
						.orElseThrow(() -> new PricingException("No selling price set for product " + productId)));
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, BigDecimal> effectiveSellingPrices(List<UUID> productIds, UUID restaurantId) {
		Map<UUID, BigDecimal> prices = new HashMap<>();
		standardSellingPriceRepository.findAllById(productIds)
				.forEach(p -> prices.put(p.getProductId(), p.getPrice()));
		restaurantPriceRepository.findByProductIdInAndRestaurantId(productIds, restaurantId)
				.forEach(p -> prices.put(p.getProductId(), p.getPrice()));
		return prices;
	}

	@Override
	@Transactional
	public PriceQuote quoteLine(QuoteLineCommand command) {
		requireProduct(command.productId());
		restaurantFacade.validateActiveRestaurant(command.restaurantId());

		BigDecimal unitPurchasePrice = purchasePriceRepository.findById(command.productId())
				.map(PurchasePrice::getPrice)
				.orElseThrow(() -> new PricingException("No purchase price set for product " + command.productId()));
		BigDecimal unitSellingPrice = effectiveSellingPrice(command.productId(), command.restaurantId());

		BigDecimal lineSubtotal = money(unitSellingPrice.multiply(command.quantity()));

		BigDecimal discountPercent = productDiscountRepository.findById(command.productId())
				.map(ProductDiscount::getDiscountPercent).orElse(BigDecimal.ZERO);
		BigDecimal discountAmount = percentOf(lineSubtotal, discountPercent);

		BigDecimal netAmount = lineSubtotal.subtract(discountAmount);

		BigDecimal taxPercent = productTaxRepository.findById(command.productId()).map(ProductTax::getTaxPercent)
				.orElse(BigDecimal.ZERO);
		BigDecimal taxAmount = percentOf(netAmount, taxPercent);

		BigDecimal serviceFeeAmount = percentOf(netAmount, currentServiceFeeRate());

		BigDecimal lineTotal = money(netAmount.add(taxAmount).add(serviceFeeAmount));

		PriceSnapshot snapshot = priceSnapshotRepository.save(PriceSnapshot.create(command.productId(),
				command.restaurantId(), command.quantity(), unitPurchasePrice, unitSellingPrice, lineSubtotal,
				discountAmount, serviceFeeAmount, taxAmount, lineTotal));
		return toQuote(snapshot);
	}

	@Override
	@Transactional(readOnly = true)
	public PriceQuote findQuote(UUID quoteId) {
		return priceSnapshotRepository.findById(quoteId).map(this::toQuote)
				.orElseThrow(() -> new PricingException("Price quote not found"));
	}

	private void requireProduct(UUID productId) {
		if (!catalogueFacade.productExists(productId)) {
			throw new PricingException("Product not found: " + productId);
		}
	}

	private ServiceFeeRule currentServiceFeeRuleEntity() {
		return serviceFeeRuleRepository.findAll().stream().findFirst()
				.orElseThrow(() -> new PricingException("No service fee rule configured"));
	}

	private DeliveryFeeRule currentDeliveryFeeRuleEntity() {
		return deliveryFeeRuleRepository.findAll().stream().findFirst()
				.orElseThrow(() -> new PricingException("No delivery fee rule configured"));
	}

	private static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
		return money(amount.multiply(percent).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
	}

	private static BigDecimal money(BigDecimal amount) {
		return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
	}

	private PriceQuote toQuote(PriceSnapshot snapshot) {
		return new PriceQuote(snapshot.getId(), snapshot.getProductId(), snapshot.getRestaurantId(),
				snapshot.getQuantity(), snapshot.getUnitPurchasePrice(), snapshot.getUnitSellingPrice(),
				snapshot.getLineSubtotal(), snapshot.getDiscountAmount(), snapshot.getServiceFeeAmount(),
				snapshot.getTaxAmount(), snapshot.getLineTotal(), snapshot.getCreatedAt());
	}
}
