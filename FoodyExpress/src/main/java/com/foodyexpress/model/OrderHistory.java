package com.foodyexpress.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OrderHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer orderHistoryId;

	@NotNull(message = "Customer id required")
	private Integer customerId;

	@OneToOne(targetEntity = Bill.class)
	private Bill bill;

}
