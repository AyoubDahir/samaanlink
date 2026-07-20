package com.foodyexpress.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class CurrentUserSession {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer currSessionId;
	
	@NotNull(message = "Email is require")
	@Email
	private String email;	
	
	@NotNull(message = "Date time is require")
	private LocalDateTime loginDateTime;
	
	@NotNull(message = "Role is require")
	private String role;

	private String privateKey;

}
