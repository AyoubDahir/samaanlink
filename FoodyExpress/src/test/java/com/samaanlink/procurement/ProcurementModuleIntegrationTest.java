package com.samaanlink.procurement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.samaanlink.AbstractIntegrationTest;
import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.catalogue.application.CreateCategoryCommand;
import com.samaanlink.catalogue.application.CreateProductCommand;
import com.samaanlink.catalogue.application.ProductSummary;
import com.samaanlink.procurement.application.AddPurchaseOrderLineCommand;
import com.samaanlink.procurement.application.CreatePurchaseOrderCommand;
import com.samaanlink.procurement.application.ProcurementException;
import com.samaanlink.procurement.application.PurchaseOrderFacade;
import com.samaanlink.procurement.application.PurchaseOrderSummary;
import com.samaanlink.supplier.application.LinkProductCommand;
import com.samaanlink.supplier.application.RegisterSupplierCommand;
import com.samaanlink.supplier.application.SupplierFacade;
import com.samaanlink.supplier.application.SupplierSummary;

/** Exercises the Procurement -> Catalogue/Supplier dependencies through a full purchase-order lifecycle. */
class ProcurementModuleIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private PurchaseOrderFacade purchaseOrderFacade;

	@Autowired
	private CatalogueFacade catalogueFacade;

	@Autowired
	private SupplierFacade supplierFacade;

	@Test
	void createsAPurchaseOrderAddsLinesAndPlacesAndReceivesIt() {
		UUID productId = createProduct("SKU-PO-1");
		UUID supplierId = createActiveSupplier("Acme Wholesale", productId);

		PurchaseOrderSummary draft = purchaseOrderFacade.createPurchaseOrder(new CreatePurchaseOrderCommand(supplierId));
		assertThat(draft.status()).isEqualTo("DRAFT");

		purchaseOrderFacade.addLine(
				new AddPurchaseOrderLineCommand(draft.id(), productId, new BigDecimal("100"), new BigDecimal("20.00")));

		PurchaseOrderSummary placed = purchaseOrderFacade.placeOrder(draft.id());
		assertThat(placed.status()).isEqualTo("PLACED");
		assertThat(placed.subtotal()).isEqualByComparingTo("2000.00");
		assertThat(placed.lines()).hasSize(1);

		PurchaseOrderSummary received = purchaseOrderFacade.receiveOrder(draft.id());
		assertThat(received.status()).isEqualTo("RECEIVED");
		assertThat(received.receivedAt()).isNotNull();

		assertThat(purchaseOrderFacade.listPurchaseOrdersBySupplier(supplierId)).extracting(PurchaseOrderSummary::id)
				.contains(draft.id());
	}

	@Test
	void cannotPlaceAPurchaseOrderWithNoLines() {
		UUID supplierId = createActiveSupplier("Empty Co", createProduct("SKU-PO-2"));
		PurchaseOrderSummary draft = purchaseOrderFacade.createPurchaseOrder(new CreatePurchaseOrderCommand(supplierId));

		assertThatThrownBy(() -> purchaseOrderFacade.placeOrder(draft.id())).isInstanceOf(ProcurementException.class);
	}

	@Test
	void cannotReceiveAPurchaseOrderThatHasNotBeenPlaced() {
		UUID supplierId = createActiveSupplier("Draft Co", createProduct("SKU-PO-3"));
		PurchaseOrderSummary draft = purchaseOrderFacade.createPurchaseOrder(new CreatePurchaseOrderCommand(supplierId));

		assertThatThrownBy(() -> purchaseOrderFacade.receiveOrder(draft.id())).isInstanceOf(ProcurementException.class);
	}

	@Test
	void cancelsADraftPurchaseOrder() {
		UUID supplierId = createActiveSupplier("Cancel Co", createProduct("SKU-PO-4"));
		PurchaseOrderSummary draft = purchaseOrderFacade.createPurchaseOrder(new CreatePurchaseOrderCommand(supplierId));

		purchaseOrderFacade.cancelOrder(draft.id());
		assertThat(purchaseOrderFacade.findPurchaseOrder(draft.id()).status()).isEqualTo("CANCELLED");
	}

	private UUID createProduct(String sku) {
		var category = catalogueFacade.createCategory(new CreateCategoryCommand("Procurement Test Category", null));
		ProductSummary product = catalogueFacade.createProduct(new CreateProductCommand("Procurement Test Product", null,
				category.id(), sku, null, "KG", "KG", BigDecimal.ONE, BigDecimal.ONE, null));
		return product.id();
	}

	private UUID createActiveSupplier(String name, UUID productId) {
		SupplierSummary supplier = supplierFacade.registerSupplier(new RegisterSupplierCommand(name, 3, 30));
		supplierFacade.linkProduct(new LinkProductCommand(supplier.id(), productId, name + "-SKU"));
		supplierFacade.activateSupplier(supplier.id());
		return supplier.id();
	}
}
