package com.foodyexpress.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

	private String message;
	private String key;
	private String role;
	private Integer customerId;

}
