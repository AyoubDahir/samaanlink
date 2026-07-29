package com.samaanlink.billing.application.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samaanlink.billing.application.BillException;
import com.samaanlink.billing.application.BillFacade;
import com.samaanlink.billing.application.BillSummary;
import com.samaanlink.billing.application.GenerateBillCommand;
import com.samaanlink.billing.domain.Bill;
import com.samaanlink.billing.infrastructure.BillRepository;
import com.samaanlink.orders.application.OrderFacade;
import com.samaanlink.orders.application.OrderSummary;

@Service
public class BillFacadeImpl implements BillFacade {

	private final BillRepository billRepository;
	private final OrderFacade orderFacade;

	public BillFacadeImpl(BillRepository billRepository, OrderFacade orderFacade) {
		this.billRepository = billRepository;
		this.orderFacade = orderFacade;
	}

	@Override
	@Transactional
	public BillSummary generateBill(GenerateBillCommand command) {
		OrderSummary order = orderFacade.findOrder(command.orderId());
		if (!"PLACED".equals(order.status())) {
			throw new BillException("Only a PLACED order can be billed");
		}
		if (billRepository.existsByOrderId(order.id())) {
			throw new BillException("Order has already been billed");
		}

		Bill bill = billRepository.save(Bill.issue(order.id(), order.restaurantId(), order.orderTotal()));
		return toSummary(bill);
	}

	@Override
	@Transactional
	public BillSummary markPaid(UUID billId) {
		Bill bill = requireBill(billId);
		if (bill.isPaid()) {
			throw new BillException("Bill has already been paid");
		}
		bill.markPaid();
		return toSummary(bill);
	}

	@Override
	@Transactional(readOnly = true)
	public BillSummary findBill(UUID billId) {
		return toSummary(requireBill(billId));
	}

	@Override
	@Transactional(readOnly = true)
	public BillSummary findByOrder(UUID orderId) {
		return billRepository.findByOrderId(orderId).map(this::toSummary)
				.orElseThrow(() -> new BillException("No bill found for order: " + orderId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<BillSummary> listByRestaurant(UUID restaurantId) {
		return billRepository.findByRestaurantId(restaurantId).stream().map(this::toSummary).toList();
	}

	private Bill requireBill(UUID billId) {
		return billRepository.findById(billId).orElseThrow(() -> new BillException("Bill not found"));
	}

	private BillSummary toSummary(Bill bill) {
		return new BillSummary(bill.getId(), bill.getOrderId(), bill.getRestaurantId(), bill.getAmount(),
				bill.getStatus().name(), bill.getCreatedAt(), bill.getPaidAt());
	}
}
