package com.samaanlink.billing.application;

import com.samaanlink.common.DomainException;

public class BillException extends DomainException {

	public BillException(String message) {
		super(message);
	}
}
