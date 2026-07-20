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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Restaurant {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer restaurantId;

	@NotNull(message = "Name is require")
	private String restaurantName;

	@ManyToOne(cascade = CascadeType.ALL)
	private Address address;

	@JsonIgnore
	@ManyToMany(targetEntity = Item.class, cascade = CascadeType.ALL, mappedBy = "restaurants")
	private List<Item> itemList = new ArrayList<>();
	private String managerName;

	@Size(min = 10, max = 10, message = "Mobile require only 10 digit")
	private String contactNunber;

}
