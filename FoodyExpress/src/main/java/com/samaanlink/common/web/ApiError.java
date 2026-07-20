package com.samaanlink.common.web;

import java.time.Instant;
import java.util.List;

/** Standard error envelope returned by every {@code /api/v1/*} endpoint on failure. */
public record ApiError(
		String code,
		String message,
		String correlationId,
		Instant timestamp,
		List<String> details) {

	public static ApiError of(String code, String message, String correlationId) {
		return new ApiError(code, message, correlationId, Instant.now(), List.of());
	}

	public static ApiError of(String code, String message, String correlationId, List<String> details) {
		return new ApiError(code, message, correlationId, Instant.now(), details);
	}
}
