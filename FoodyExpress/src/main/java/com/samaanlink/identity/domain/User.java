package com.samaanlink.identity.domain;

import java.util.UUID;

import com.samaanlink.common.audit.Auditable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users", schema = "identity")
public class User extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String email;

	private String passwordHash;

	private String firstName;

	private String lastName;

	private String phone;

	@ManyToOne
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	private UserStatus status;

	protected User() {
	}

	public static User create(String email, String passwordHash, String firstName, String lastName, String phone,
			Role role) {
		User user = new User();
		user.email = email;
		user.passwordHash = passwordHash;
		user.firstName = firstName;
		user.lastName = lastName;
		user.phone = phone;
		user.role = role;
		user.status = UserStatus.ACTIVE;
		return user;
	}

	public void changePassword(String newPasswordHash) {
		this.passwordHash = newPasswordHash;
	}

	public void activate() {
		this.status = UserStatus.ACTIVE;
	}

	public void suspend() {
		this.status = UserStatus.SUSPENDED;
	}

	public boolean isActive() {
		return status == UserStatus.ACTIVE;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPhone() {
		return phone;
	}

	public Role getRole() {
		return role;
	}

	public UserStatus getStatus() {
		return status;
	}
}
