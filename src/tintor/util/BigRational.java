package tintor.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BigRational implements Comparable<BigRational> {
	public static final BigRational ONE = new BigRational(BigInteger.ONE, BigInteger.ONE);
	public static final BigRational ZERO = new BigRational(BigInteger.ZERO, BigInteger.ONE);

	public static BigRational create(final double d) {
		final BigDecimal b = BigDecimal.valueOf(d);
		if (b.scale() >= 0)
			return create(b.unscaledValue(), BigInteger.TEN.pow(b.scale()));
		return create(b.unscaledValue().multiply(BigInteger.TEN.pow(-b.scale())));
	}

	public static BigRational create(final long p) {
		return create(BigInteger.valueOf(p));
	}

	public static BigRational create(final BigInteger p) {
		return new BigRational(p, BigInteger.ONE);
	}

	public static BigRational create(final long p, final long q) {
		return create(BigInteger.valueOf(p), BigInteger.valueOf(q));
	}

	public static BigRational create(final BigInteger p, final BigInteger q) {
		final BigInteger a = q.signum() < 0 ? p.negate().gcd(q.negate()) : p.gcd(q);
		return new BigRational(p.divide(a), q.divide(a));
	}

	private final BigInteger p, q;

	private BigRational(final BigInteger pa, final BigInteger qa) {
		if (qa.signum() == 0)
			throw new IllegalArgumentException("division by zero");
		assert qa.signum() > 0;
		assert pa.gcd(qa).compareTo(BigInteger.ONE) == 0;
		p = pa;
		q = qa;
	}

	public BigRational add(final BigRational v) {
		return create(p.multiply(v.q).add(q.multiply(v.p)), q.multiply(v.q));
	}

	public BigRational add(final long v) {
		return create(p.add(q.multiply(BigInteger.valueOf(v))), q);
	}

	public BigRational subtract(final BigRational v) {
		return create(p.multiply(v.q).subtract(q.multiply(v.p)), q.multiply(v.q));
	}

	public BigRational subtract(final long v) {
		return create(p.subtract(q.multiply(BigInteger.valueOf(v))), q);
	}

	public BigRational multiply(final BigRational v) {
		return create(p.multiply(v.p), q.multiply(v.q));
	}

	public BigRational multiply(final long v) {
		return create(p.multiply(BigInteger.valueOf(v)), q);
	}

	public BigRational divide(final BigRational v) {
		return create(p.multiply(v.q), q.multiply(v.p));
	}

	public BigRational divide(final long v) {
		return create(p, q.multiply(BigInteger.valueOf(v)));
	}

	public BigRational abs() {
		return p.signum() < 0 ? new BigRational(p.negate(), q) : this;
	}

	public BigRational negate() {
		return new BigRational(p.negate(), q);
	}

	public BigRational invert() {
		return p.signum() < 0 ? new BigRational(q.negate(), p.negate()) : new BigRational(q, p);
	}

	public int signum() {
		return p.signum();
	}

	public BigRational max(final BigRational v) {
		return compareTo(v) > 0 ? this : v;
	}

	public BigRational min(final BigRational v) {
		return compareTo(v) < 0 ? this : v;
	}

	public double doubleValue() {
		if (q.compareTo(BigInteger.ONE) == 0)
			return p.doubleValue();
		return new BigDecimal(p).divide(new BigDecimal(q)).doubleValue();
	}

	public long longValue() {
		if (q.compareTo(BigInteger.ONE) == 0)
			return p.longValue();
		return new BigDecimal(p).divide(new BigDecimal(q)).longValue();
	}

	public BigInteger nominator() {
		return p;
	}

	public BigInteger denominator() {
		return q;
	}

	public BigInteger integer() {
		return p.divide(q);
	}

	// Note: result is negative for negative inputs 
	public BigRational fraction() {
		return new BigRational(p.remainder(q), q);
	}

	@Override
	public String toString() {
		if (q.compareTo(BigInteger.ONE) == 0)
			return p.toString();

		final BigInteger[] d = p.divideAndRemainder(q);
		if (d[0].signum() == 0)
			return d[1] + "/" + q;
		return String.format("%s[%s/%s]", d[0], d[1].abs(), q);
	}

	@Override
	public int compareTo(final BigRational v) {
		return p.multiply(v.q).compareTo(q.multiply(v.p));
	}

	public int compareTo(final long v) {
		return p.compareTo(q.multiply(BigInteger.valueOf(v)));
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof BigRational ? equals((BigRational) o) : false;
	}

	public boolean equals(final BigRational v) {
		return p.equals(v.p) && q.equals(v.q);
	}

	@Override
	public int hashCode() {
		return Hash.hash(p.hashCode(), q.hashCode());
	}
}
