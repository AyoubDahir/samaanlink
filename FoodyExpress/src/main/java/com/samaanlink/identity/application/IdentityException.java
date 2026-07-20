package com.samaanlink.identity.application;

import com.samaanlink.common.DomainException;

/** Thrown for any business-rule violation in the Identity module (bad credentials, unknown role, expired token, ...). */
public class IdentityException extends DomainException {

	public IdentityException(String message) {
		super(message);
	}
}
