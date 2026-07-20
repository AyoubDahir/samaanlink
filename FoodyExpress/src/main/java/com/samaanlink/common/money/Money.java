package com.samaanlink.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable monetary value. Every module that handles money (Pricing, Procurement, Orders,
 * Payments, Accounting, Reporting) uses this instead of a bare {@link BigDecimal} so that scale
 * and rounding are applied consistently everywhere a customer- or ledger-facing amount is
 * produced.
 *
 * <p>Fixed at 2 decimal places, {@link RoundingMode#HALF_UP} — the rounding behaviour implied by
 * the SamaanLink pricing example (purchase $5.00 + margin $0.50 + 5% service fee + $1.00 delivery
 * = 6.775, rounded to 6.78).
 */
public final class Money implements Comparable<Money> {

	private static final int SCALE = 2;
	private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

	public static final Money ZERO = Money.of(BigDecimal.ZERO);

	private final BigDecimal amount;

	private Money(BigDecimal amount) {
		this.amount = amount.setScale(SCALE, ROUNDING);
	}

	public static Money of(BigDecimal amount) {
		Objects.requireNonNull(amount, "amount");
		return new Money(amount);
	}

	public static Money of(String amount) {
		return of(new BigDecimal(amount));
	}

	public BigDecimal amount() {
		return amount;
	}

	public Money add(Money other) {
		return new Money(this.amount.add(other.amount));
	}

	public Money subtract(Money other) {
		return new Money(this.amount.subtract(other.amount));
	}

	public Money multiply(BigDecimal factor) {
		return new Money(this.amount.multiply(factor));
	}

	/** {@code percentage(new BigDecimal("5"))} returns 5% of this amount. */
	public Money percentage(BigDecimal percent) {
		return new Money(this.amount.multiply(percent).divide(BigDecimal.valueOf(100), 10, ROUNDING));
	}

	public boolean isNegative() {
		return amount.signum() < 0;
	}

	public boolean isZero() {
		return amount.signum() == 0;
	}

	@Override
	public int compareTo(Money other) {
		return this.amount.compareTo(other.amount);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Money other)) return false;
		return amount.equals(other.amount);
	}

	@Override
	public int hashCode() {
		return amount.hashCode();
	}

	@Override
	public String toString() {
		return amount.toPlainString();
	}
}
