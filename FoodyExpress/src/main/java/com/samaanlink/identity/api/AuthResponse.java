package com.samaanlink.identity.api;

import java.util.UUID;

import com.samaanlink.identity.application.AuthResult;

public record AuthResponse(
		String accessToken,
		String refreshToken,
		long expiresInSeconds,
		UUID userId,
		String roleName) {

	public static AuthResponse from(AuthResult result) {
		return new AuthResponse(result.accessToken(), result.refreshToken(), result.expiresInSeconds(),
				result.userId(), result.roleName());
	}
}
