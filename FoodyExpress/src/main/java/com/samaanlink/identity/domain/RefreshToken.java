package com.samaanlink.identity.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens", schema = "identity")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private UUID userId;

	private String tokenHash;

	private Instant expiresAt;

	private Instant revokedAt;

	protected RefreshToken() {
	}

	public static RefreshToken issue(UUID userId, String tokenHash, Instant expiresAt) {
		RefreshToken token = new RefreshToken();
		token.userId = userId;
		token.tokenHash = tokenHash;
		token.expiresAt = expiresAt;
		return token;
	}

	public boolean isUsable(Instant now) {
		return revokedAt == null && now.isBefore(expiresAt);
	}

	public void revoke() {
		this.revokedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}
}
