package com.foodyexpress.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer adminId;

	@NotNull(message = "Name is required")
	private String name;
	
	@NotNull(message = "Email is required")
	@Email(message =  "Email format require")
	private String email;
	
	@Size(min = 6, max = 16, message = "Password should between 6 to 16 character")
	private String password;

}
