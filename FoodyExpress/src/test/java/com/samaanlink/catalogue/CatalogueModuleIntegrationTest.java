package com.samaanlink.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.samaanlink.AbstractIntegrationTest;
import com.samaanlink.catalogue.application.CatalogueException;
import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.catalogue.application.CategorySummary;
import com.samaanlink.catalogue.application.CreateCategoryCommand;
import com.samaanlink.catalogue.application.CreateProductCommand;
import com.samaanlink.catalogue.application.ProductSummary;

class CatalogueModuleIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private CatalogueFacade catalogueFacade;

	@Test
	void createsAProductUnderACategoryAndCanRetrieveIt() {
		CategorySummary category = catalogueFacade.createCategory(new CreateCategoryCommand("Produce", null));

		ProductSummary product = catalogueFacade.createProduct(new CreateProductCommand("Banana", "Fresh bananas",
				category.id(), "SKU-BANANA-40KG", "0123456789", "KG", "KG", new BigDecimal("40"),
				new BigDecimal("40"), new BigDecimal("40")));

		assertThat(product.status()).isEqualTo("DRAFT");
		assertThat(catalogueFacade.findProduct(product.id()).sku()).isEqualTo("SKU-BANANA-40KG");
		assertThat(catalogueFacade.productExists(product.id())).isTrue();

		catalogueFacade.activateProduct(product.id());
		assertThat(catalogueFacade.findProduct(product.id()).status()).isEqualTo("ACTIVE");

		assertThat(catalogueFacade.listCategories()).extracting(CategorySummary::id).contains(category.id());
	}

	@Test
	void rejectsADuplicateSku() {
		CategorySummary category = catalogueFacade.createCategory(new CreateCategoryCommand("Dairy", null));
		CreateProductCommand command = new CreateProductCommand("Milk", null, category.id(), "SKU-MILK-1L", null,
				"L", "L", BigDecimal.ONE, BigDecimal.ONE, null);

		catalogueFacade.createProduct(command);

		assertThatThrownBy(() -> catalogueFacade.createProduct(command)).isInstanceOf(CatalogueException.class);
	}

	@Test
	void deletesAProductThatHasNoOtherReferences() {
		CategorySummary category = catalogueFacade.createCategory(new CreateCategoryCommand("Deletable", null));
		ProductSummary product = catalogueFacade.createProduct(new CreateProductCommand("Throwaway", null,
				category.id(), "SKU-DELETE-1", null, "KG", "KG", BigDecimal.ONE, BigDecimal.ONE, null));

		catalogueFacade.deleteProduct(product.id());

		assertThat(catalogueFacade.productExists(product.id())).isFalse();
	}

	@Test
	void cannotDeleteACategoryThatStillHasProducts() {
		CategorySummary category = catalogueFacade.createCategory(new CreateCategoryCommand("Non-empty", null));
		catalogueFacade.createProduct(new CreateProductCommand("Blocker", null, category.id(), "SKU-BLOCKER-1", null,
				"KG", "KG", BigDecimal.ONE, BigDecimal.ONE, null));

		assertThatThrownBy(() -> catalogueFacade.deleteCategory(category.id())).isInstanceOf(CatalogueException.class);
	}

	@Test
	void cannotDeleteACategoryThatStillHasSubcategories() {
		CategorySummary parent = catalogueFacade.createCategory(new CreateCategoryCommand("Parent", null));
		catalogueFacade.createCategory(new CreateCategoryCommand("Child", parent.id()));

		assertThatThrownBy(() -> catalogueFacade.deleteCategory(parent.id())).isInstanceOf(CatalogueException.class);
	}

	@Test
	void deletesAnEmptyCategory() {
		CategorySummary category = catalogueFacade.createCategory(new CreateCategoryCommand("Empty", null));

		catalogueFacade.deleteCategory(category.id());

		assertThat(catalogueFacade.listCategories()).extracting(CategorySummary::id).doesNotContain(category.id());
	}
}
