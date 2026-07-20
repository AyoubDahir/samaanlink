package com.samaanlink.identity.application;

import java.util.UUID;

public record AuthResult(
		String accessToken,
		String refreshToken,
		long expiresInSeconds,
		UUID userId,
		String roleName) {
}
