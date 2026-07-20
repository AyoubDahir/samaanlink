package com.foodyexpress.model;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Bill {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer billId;
	
	@NotNull(message = "Date time required")
	private LocalDateTime billDate;
	
	@NotNull(message = "Total cost required")
	private Double totalCost;
	
	@NotNull(message = "Total item required")
	private Integer totalItem;
	
	@OneToOne
	private OrderDetails order;

}