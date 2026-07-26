package com.samaanlink.orders.application;

import com.samaanlink.common.DomainException;

public class OrderException extends DomainException {

	public OrderException(String message) {
		super(message);
	}
}
