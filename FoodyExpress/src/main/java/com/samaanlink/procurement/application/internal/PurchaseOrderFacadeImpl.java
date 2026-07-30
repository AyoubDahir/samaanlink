package com.samaanlink.procurement.application.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samaanlink.catalogue.application.CatalogueFacade;
import com.samaanlink.procurement.application.AddPurchaseOrderLineCommand;
import com.samaanlink.procurement.application.CreatePurchaseOrderCommand;
import com.samaanlink.procurement.application.ProcurementException;
import com.samaanlink.procurement.application.PurchaseOrderFacade;
import com.samaanlink.procurement.application.PurchaseOrderLineSummary;
import com.samaanlink.procurement.application.PurchaseOrderSummary;
import com.samaanlink.procurement.domain.PurchaseOrder;
import com.samaanlink.procurement.domain.PurchaseOrderLine;
import com.samaanlink.procurement.infrastructure.PurchaseOrderLineRepository;
import com.samaanlink.procurement.infrastructure.PurchaseOrderRepository;
import com.samaanlink.supplier.application.SupplierFacade;

@Service
public class PurchaseOrderFacadeImpl implements PurchaseOrderFacade {

	private final PurchaseOrderRepository purchaseOrderRepository;
	private final PurchaseOrderLineRepository purchaseOrderLineRepository;
	private final CatalogueFacade catalogueFacade;
	private final SupplierFacade supplierFacade;

	public PurchaseOrderFacadeImpl(PurchaseOrderRepository purchaseOrderRepository,
			PurchaseOrderLineRepository purchaseOrderLineRepository, CatalogueFacade catalogueFacade,
			SupplierFacade supplierFacade) {
		this.purchaseOrderRepository = purchaseOrderRepository;
		this.purchaseOrderLineRepository = purchaseOrderLineRepository;
		this.catalogueFacade = catalogueFacade;
		this.supplierFacade = supplierFacade;
	}

	@Override
	@Transactional
	public PurchaseOrderSummary createPurchaseOrder(CreatePurchaseOrderCommand command) {
		supplierFacade.findSupplier(command.supplierId());
		PurchaseOrder purchaseOrder = purchaseOrderRepository.save(PurchaseOrder.createDraft(command.supplierId()));
		return toSummary(purchaseOrder);
	}

	@Override
	@Transactional
	public PurchaseOrderLineSummary addLine(AddPurchaseOrderLineCommand command) {
		PurchaseOrder purchaseOrder = requirePurchaseOrder(command.purchaseOrderId());
		requireDraft(purchaseOrder);
		if (!catalogueFacade.productExists(command.productId())) {
			throw new ProcurementException("Product not found: " + command.productId());
		}

		PurchaseOrderLine line = purchaseOrderLineRepository.save(PurchaseOrderLine.of(purchaseOrder,
				command.productId(), command.quantity(), command.unitCost()));
		return toSummary(line);
	}

	@Override
	@Transactional
	public void removeLine(UUID purchaseOrderId, UUID lineId) {
		PurchaseOrder purchaseOrder = requirePurchaseOrder(purchaseOrderId);
		requireDraft(purchaseOrder);
		purchaseOrderLineRepository.deleteByIdAndPurchaseOrderId(lineId, purchaseOrderId);
	}

	@Override
	@Transactional
	public PurchaseOrderSummary placeOrder(UUID purchaseOrderId) {
		PurchaseOrder purchaseOrder = requirePurchaseOrder(purchaseOrderId);
		requireDraft(purchaseOrder);
		supplierFacade.validateActiveSupplier(purchaseOrder.getSupplierId());

		List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(purchaseOrderId);
		if (lines.isEmpty()) {
			throw new ProcurementException("Cannot place a purchase order with no lines");
		}

		BigDecimal subtotal = lines.stream().map(PurchaseOrderLine::getLineTotal).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		purchaseOrder.place(subtotal);
		return toSummary(purchaseOrder, lines);
	}

	@Override
	@Transactional
	public PurchaseOrderSummary receiveOrder(UUID purchaseOrderId) {
		PurchaseOrder purchaseOrder = requirePurchaseOrder(purchaseOrderId);
		if (!purchaseOrder.isPlaced()) {
			throw new ProcurementException("Only a PLACED purchase order can be received");
		}
		purchaseOrder.receive();
		return toSummary(purchaseOrder);
	}

	@Override
	@Transactional
	public void cancelOrder(UUID purchaseOrderId) {
		PurchaseOrder purchaseOrder = requirePurchaseOrder(purchaseOrderId);
		if (!purchaseOrder.isDraft() && !purchaseOrder.isPlaced()) {
			throw new ProcurementException("Only a DRAFT or PLACED purchase order can be cancelled");
		}
		purchaseOrder.cancel();
	}

	@Override
	@Transactional(readOnly = true)
	public PurchaseOrderSummary findPurchaseOrder(UUID purchaseOrderId) {
		PurchaseOrder purchaseOrder = requirePurchaseOrder(purchaseOrderId);
		return toSummary(purchaseOrder, purchaseOrderLineRepository.findByPurchaseOrderId(purchaseOrderId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PurchaseOrderSummary> listAllPurchaseOrders() {
		return purchaseOrderRepository.findAll().stream()
				.map(po -> toSummary(po, purchaseOrderLineRepository.findByPurchaseOrderId(po.getId()))).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<PurchaseOrderSummary> listPurchaseOrdersBySupplier(UUID supplierId) {
		return purchaseOrderRepository.findBySupplierId(supplierId).stream()
				.map(po -> toSummary(po, purchaseOrderLineRepository.findByPurchaseOrderId(po.getId()))).toList();
	}

	private PurchaseOrder requirePurchaseOrder(UUID purchaseOrderId) {
		return purchaseOrderRepository.findById(purchaseOrderId)
				.orElseThrow(() -> new ProcurementException("Purchase order not found"));
	}

	private void requireDraft(PurchaseOrder purchaseOrder) {
		if (!purchaseOrder.isDraft()) {
			throw new ProcurementException("Purchase order is not in DRAFT status");
		}
	}

	private PurchaseOrderSummary toSummary(PurchaseOrder purchaseOrder) {
		return toSummary(purchaseOrder, purchaseOrderLineRepository.findByPurchaseOrderId(purchaseOrder.getId()));
	}

	private PurchaseOrderSummary toSummary(PurchaseOrder purchaseOrder, List<PurchaseOrderLine> lines) {
		return new PurchaseOrderSummary(purchaseOrder.getId(), purchaseOrder.getSupplierId(),
				purchaseOrder.getStatus().name(), purchaseOrder.getSubtotal(), purchaseOrder.getCreatedAt(),
				purchaseOrder.getPlacedAt(), purchaseOrder.getReceivedAt(), lines.stream().map(this::toSummary).toList());
	}

	private PurchaseOrderLineSummary toSummary(PurchaseOrderLine line) {
		return new PurchaseOrderLineSummary(line.getId(), line.getProductId(), line.getQuantity(),
				line.getUnitCost(), line.getLineTotal());
	}
}
