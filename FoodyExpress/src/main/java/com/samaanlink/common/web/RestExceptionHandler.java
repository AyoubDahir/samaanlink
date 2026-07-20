package com.samaanlink.common.web;

import java.util.List;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.samaanlink.common.DomainException;

/**
 * Maps every {@code com.samaanlink.**} module's exceptions to the standard {@link ApiError}
 * envelope. {@code @Order(HIGHEST_PRECEDENCE)} ensures this wins over the legacy
 * {@code com.foodyexpress.exception.GlobalExceptionHandler}'s catch-all {@code Exception} handler,
 * which would otherwise also match (Spring's {@code @ControllerAdvice} applies globally by
 * default, regardless of package) and return the legacy {@code MyErrorDetails} shape instead.
 */
@RestControllerAdvice(basePackages = "com.samaanlink")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ApiError> handleDomainException(DomainException ex) {
		return ResponseEntity.badRequest().body(ApiError.of("DOMAIN_ERROR", ex.getMessage(), correlationId()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
				.toList();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiError.of("VALIDATION_ERROR", "Request validation failed", correlationId(), details));
	}

	private static String correlationId() {
		String id = MDC.get(CorrelationIdFilter.MDC_KEY);
		return id != null ? id : "";
	}
}
