package com.samaanlink.supplier.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "supplier_contacts", schema = "supplier")
public class SupplierContact {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "supplier_id", nullable = false)
	private Supplier supplier;

	private String name;

	private String phone;

	private String email;

	private String roleTitle;

	protected SupplierContact() {
	}

	public static SupplierContact create(Supplier supplier, String name, String phone, String email,
			String roleTitle) {
		SupplierContact contact = new SupplierContact();
		contact.supplier = supplier;
		contact.name = name;
		contact.phone = phone;
		contact.email = email;
		contact.roleTitle = roleTitle;
		return contact;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public String getRoleTitle() {
		return roleTitle;
	}
}
