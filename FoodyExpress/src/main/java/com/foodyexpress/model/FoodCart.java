package com.foodyexpress.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

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
