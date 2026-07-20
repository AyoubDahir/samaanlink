package com.samaanlink.identity.application;

import java.util.UUID;

/** The only shape of a SamaanLink user other modules are allowed to see - never the User entity itself. */
public record UserSummary(
		UUID userId,
		String email,
		String firstName,
		String lastName,
		String roleName,
		String status) {
}
