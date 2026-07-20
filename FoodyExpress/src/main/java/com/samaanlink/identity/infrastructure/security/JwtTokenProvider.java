package com.samaanlink.identity.infrastructure.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.samaanlink.identity.domain.Permission;
import com.samaanlink.identity.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Issues and parses the access and refresh JWTs. Access tokens carry the role name and permission
 * codes as claims so that request-time authorization needs no database round trip; refresh tokens
 * carry only the subject and are additionally tracked server-side (see {@link RefreshTokenRepository})
 * so they can be revoked.
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenProvider {

	private final JwtProperties properties;
	private final SecretKey key;

	public JwtTokenProvider(JwtProperties properties) {
		this.properties = properties;
		this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.getSecret()));
	}

	public String generateAccessToken(User user) {
		Instant now = Instant.now();
		List<String> permissionCodes = user.getRole().getPermissions().stream()
				.map(Permission::getCode)
				.collect(Collectors.toList());

		return Jwts.builder()
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim("role", user.getRole().getName())
				.claim("permissions", permissionCodes)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(properties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES)))
				.signWith(key)
				.compact();
	}

	public String generateRefreshToken(User user) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(user.getId().toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(properties.getRefreshTokenTtlDays(), ChronoUnit.DAYS)))
				.signWith(key)
				.compact();
	}

	public Instant refreshTokenExpiry() {
		return Instant.now().plus(properties.getRefreshTokenTtlDays(), ChronoUnit.DAYS);
	}

	public Instant accessTokenExpiry() {
		return Instant.now().plus(properties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES);
	}

	public long accessTokenTtlSeconds() {
		return properties.getAccessTokenTtlMinutes() * 60;
	}

	/** @throws io.jsonwebtoken.JwtException if the token is malformed, expired, or has an invalid signature */
	public Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	public UUID subject(Claims claims) {
		return UUID.fromString(claims.getSubject());
	}
}
