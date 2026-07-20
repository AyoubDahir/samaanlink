package com.foodyexpress.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer customerId;
	
	@NotNull(message = "First name is mandatory")
	private String firstName;
	
	@NotNull(message = "Last name is mandatory")
	private String lastName;
	
	private Integer age;
	private String gender;
	
	@Size(max = 10,min = 10, message = "Require only 10 digit")
	private String mobileNumber;
	
	@NotNull(message = "Email is mandatory")
	@Email(message = "Require email format")
	private String email;
	
	@NotNull(message = "Password is mandatory")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@OneToOne(targetEntity = FoodCart.class)
	private FoodCart cart;

}