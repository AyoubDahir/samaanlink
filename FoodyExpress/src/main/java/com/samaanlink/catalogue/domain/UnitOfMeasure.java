package com.samaanlink.catalogue.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "units_of_measure", schema = "catalogue")
public class UnitOfMeasure {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String code;

	private String name;

	protected UnitOfMeasure() {
	}

	public UUID getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
