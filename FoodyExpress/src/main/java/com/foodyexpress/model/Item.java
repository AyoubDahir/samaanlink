package com.foodyexpress.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer itemId;
	
	@NotNull(message = "Name is require")
	private String itemName;
	
	@NotNull(message = "Category is require")
	@ManyToOne
	private Category category;

	private Integer quantity;
	private Double cost;

	@JsonIgnore
	@ManyToMany(targetEntity = Restaurant.class)
	private List<Restaurant> restaurants = new ArrayList<>();

}
