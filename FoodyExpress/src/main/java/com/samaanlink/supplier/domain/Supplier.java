package com.samaanlink.supplier.domain;

import java.util.UUID;

import com.samaanlink.common.audit.Auditable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "suppliers", schema = "supplier")
public class Supplier extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String name;

	private int leadTimeDays;

	private int paymentTermDays;

	@Enumerated(EnumType.STRING)
	private SupplierStatus status;

	protected Supplier() {
	}

	public static Supplier create(String name, int leadTimeDays, int paymentTermDays) {
		Supplier supplier = new Supplier();
		supplier.name = name;
		supplier.leadTimeDays = leadTimeDays;
		supplier.paymentTermDays = paymentTermDays;
		supplier.status = SupplierStatus.PENDING;
		return supplier;
	}

	public void activate() {
		this.status = SupplierStatus.ACTIVE;
	}

	public void suspend() {
		this.status = SupplierStatus.SUSPENDED;
	}

	public void deactivate() {
		this.status = SupplierStatus.INACTIVE;
	}

	public boolean isActive() {
		return status == SupplierStatus.ACTIVE;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getLeadTimeDays() {
		return leadTimeDays;
	}

	public int getPaymentTermDays() {
		return paymentTermDays;
	}

	public SupplierStatus getStatus() {
		return status;
	}
}
