package com.samaanlink.identity.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions", schema = "identity")
public class Permission {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String code;

	private String description;

	protected Permission() {
	}

	public UUID getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
}
