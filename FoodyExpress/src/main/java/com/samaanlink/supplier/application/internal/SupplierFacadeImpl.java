package com.samaanlink.supplier.application.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.supplier.application.AddSupplierContactCommand;
import com.samaanlink.supplier.application.LinkProductCommand;
import com.samaanlink.supplier.application.RegisterSupplierCommand;
import com.samaanlink.supplier.application.SupplierException;
import com.samaanlink.supplier.application.SupplierFacade;
import com.samaanlink.supplier.application.SupplierSummary;
import com.samaanlink.supplier.domain.Supplier;
import com.samaanlink.supplier.domain.SupplierContact;
import com.samaanlink.supplier.domain.SupplierProduct;
import com.samaanlink.supplier.infrastructure.SupplierContactRepository;
import com.samaanlink.supplier.infrastructure.SupplierProductRepository;
import com.samaanlink.supplier.infrastructure.SupplierRepository;

@Service
public class SupplierFacadeImpl implements SupplierFacade {

	private final SupplierRepository supplierRepository;
	private final SupplierContactRepository supplierContactRepository;
	private final SupplierProductRepository supplierProductRepository;
	private final CatalogueFacade catalogueFacade;

	public SupplierFacadeImpl(SupplierRepository supplierRepository,
			SupplierContactRepository supplierContactRepository, SupplierProductRepository supplierProductRepository,
			CatalogueFacade catalogueFacade) {
		this.supplierRepository = supplierRepository;
		this.supplierContactRepository = supplierContactRepository;
		this.supplierProductRepository = supplierProductRepository;
		this.catalogueFacade = catalogueFacade;
	}

	@Override
	@Transactional
	public SupplierSummary registerSupplier(RegisterSupplierCommand command) {
		Supplier supplier = supplierRepository
				.save(Supplier.create(command.name(), command.leadTimeDays(), command.paymentTermDays()));
		return toSummary(supplier);
	}

	@Override
	@Transactional
	public void addContact(AddSupplierContactCommand command) {
		Supplier supplier = supplierRepository.findById(command.supplierId())
				.orElseThrow(() -> new SupplierException("Supplier not found"));
		supplierContactRepository
				.save(SupplierContact.create(supplier, command.name(), command.phone(), command.email(),
						command.roleTitle()));
	}

	@Override
	@Transactional
	public void linkProduct(LinkProductCommand command) {
		Supplier supplier = supplierRepository.findById(command.supplierId())
				.orElseThrow(() -> new SupplierException("Supplier not found"));
		if (!catalogueFacade.productExists(command.productId())) {
			throw new SupplierException("Product not found in catalogue");
		}
		if (supplierProductRepository.existsBySupplierIdAndProductId(command.supplierId(), command.productId())) {
			throw new SupplierException("This product is already linked to this supplier");
		}
		supplierProductRepository.save(SupplierProduct.link(supplier, command.productId(), command.supplierSku()));
	}

	@Override
	@Transactional
	public void activateSupplier(UUID supplierId) {
		supplierRepository.findById(supplierId).orElseThrow(() -> new SupplierException("Supplier not found"))
				.activate();
	}

	@Override
	@Transactional
	public void suspendSupplier(UUID supplierId) {
		supplierRepository.findById(supplierId).orElseThrow(() -> new SupplierException("Supplier not found"))
				.suspend();
	}

	@Override
	@Transactional(readOnly = true)
	public SupplierSummary findSupplier(UUID supplierId) {
		return supplierRepository.findById(supplierId).map(this::toSummary)
				.orElseThrow(() -> new SupplierException("Supplier not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public List<SupplierSummary> listSuppliersForProduct(UUID productId) {
		return supplierProductRepository.findByProductId(productId).stream()
				.map(link -> supplierRepository.findById(link.getSupplier().getId()))
				.flatMap(java.util.Optional::stream)
				.map(this::toSummary)
				.toList();
	}

	private SupplierSummary toSummary(Supplier supplier) {
		return new SupplierSummary(supplier.getId(), supplier.getName(), supplier.getLeadTimeDays(),
				supplier.getPaymentTermDays(), supplier.getStatus().name());
	}
}
