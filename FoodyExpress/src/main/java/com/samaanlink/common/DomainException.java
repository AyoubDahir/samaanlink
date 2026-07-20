package com.samaanlink.common;

/** Base type for business-rule violations raised by any module's application layer. */
public class DomainException extends RuntimeException {

	public DomainException(String message) {
		super(message);
	}
}
