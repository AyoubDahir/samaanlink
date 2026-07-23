package com.samaanlink.supplier;

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
import com.samaanlink.supplier.application.LinkProductCommand;
import com.samaanlink.supplier.application.RegisterSupplierCommand;
import com.samaanlink.supplier.application.SupplierException;
import com.samaanlink.supplier.application.SupplierFacade;
import com.samaanlink.supplier.application.SupplierSummary;

/** Exercises the deliberate Supplier -> Catalogue dependency added for product-existence validation on link. */
class SupplierModuleIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private SupplierFacade supplierFacade;

	@Autowired
	private CatalogueFacade catalogueFacade;

	@Test
	void registersASupplierAndLinksAnExistingProduct() {
		var category = catalogueFacade.createCategory(new CreateCategoryCommand("Grains", null));
		ProductSummary product = catalogueFacade.createProduct(new CreateProductCommand("Rice", null, category.id(),
				"SKU-RICE-25KG", null, "KG", "KG", new BigDecimal("25"), new BigDecimal("25"), null));

		SupplierSummary supplier = supplierFacade
				.registerSupplier(new RegisterSupplierCommand("Acme Wholesale", 3, 30));
		assertThat(supplier.status()).isEqualTo("PENDING");

		supplierFacade.linkProduct(new LinkProductCommand(supplier.id(), product.id(), "ACME-RICE-25"));

		assertThat(supplierFacade.listSuppliersForProduct(product.id()))
				.extracting(SupplierSummary::id)
				.containsExactly(supplier.id());
	}

	@Test
	void rejectsLinkingAProductThatDoesNotExistInTheCatalogue() {
		SupplierSummary supplier = supplierFacade.registerSupplier(new RegisterSupplierCommand("Ghost Co", 1, 0));

		assertThatThrownBy(() -> supplierFacade.linkProduct(new LinkProductCommand(supplier.id(), UUID.randomUUID(), null)))
				.isInstanceOf(SupplierException.class);
	}
}
