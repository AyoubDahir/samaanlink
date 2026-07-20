package com.samaanlink.identity.application;

/** {@code roleName} must match one of the ten fixed role names seeded in identity.roles. */
public record RegisterUserCommand(
		String email,
		String rawPassword,
		String firstName,
		String lastName,
		String phone,
		String roleName) {
}
