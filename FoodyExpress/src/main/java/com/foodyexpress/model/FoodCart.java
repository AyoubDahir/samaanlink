package com.foodyexpress.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

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
public class FoodCart {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer cartId;

	@JsonIgnore
	@OneToOne(targetEntity = Customer.class, mappedBy = "cart")
	private Customer customer;

	/**
	 * Modeled many-to-many (not one-to-many) so the same menu item can sit in more than one
	 * cart at once — a unidirectional @OneToMany via join table makes Hibernate add a unique
	 * constraint on the item FK, which fails as soon as two carts reference the same item.
	 */
	@ManyToMany(targetEntity = Item.class)
	private List<Item> itemList = new ArrayList<>();
	
	
}
