package com.samaanlink.identity.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "samaanlink.security.jwt")
public class JwtProperties {

	/** Base64-encoded HMAC-SHA256 signing key. Must be overridden via env var outside local dev. */
	private String secret = "ZGV2LW9ubHktc2FtYWFubGluay1qd3Qtc2lnbmluZy1rZXktY2hhbmdlLW1l";

	private long accessTokenTtlMinutes = 15;

	private long refreshTokenTtlDays = 7;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getAccessTokenTtlMinutes() {
		return accessTokenTtlMinutes;
	}

	public void setAccessTokenTtlMinutes(long accessTokenTtlMinutes) {
		this.accessTokenTtlMinutes = accessTokenTtlMinutes;
	}

	public long getRefreshTokenTtlDays() {
		return refreshTokenTtlDays;
	}

	public void setRefreshTokenTtlDays(long refreshTokenTtlDays) {
		this.refreshTokenTtlDays = refreshTokenTtlDays;
	}
}
