package tintor.apps.scanner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

class IntervalTest {
	public static void main(final String[] args) {
		System.out.println(BigInteger.valueOf(-20).gcd(BigInteger.valueOf(12)));
	}

	public static void main2(final String[] args) {
		long i = 0;
		final Random rand = new Random();
		while (true) {
			final double a = randFinite(rand);
			final double b = randFinite(rand);
			final double c = randFinite(rand);
			final double d = randFinite(rand);

			final Interval det = Interval.sub(Interval.mul(a, b), Interval.mul(c, d));
			if (det.contains(0.0)) {
				System.out.printf("a=%s, b=%s, c=%s, d=%s, det=%s\n", a, b, c, d, det);
			}

			i++;
		}
	}

	static double randFinite(final Random rand) {
		while (true) {
			final double a = Double.longBitsToDouble(rand.nextLong());
			if (!Double.isInfinite(a) && !Double.isNaN(a))
				return a;
		}
	}

	// sign of a * b - c * d
	static int signOfDeterminant(final long a, final long b, final long c, final long d) {
		final BigInteger ab = BigInteger.valueOf(a).multiply(BigInteger.valueOf(b));
		final BigInteger cd = BigInteger.valueOf(c).multiply(BigInteger.valueOf(d));
		return ab.subtract(cd).signum();
	}

	// sign of a * b - c * d
	static int signOfDeterminant(final double a, final double b, final double c, final double d) {
		if ((a == 0.0 || b == 0.0) && (c == 0.0 || d == 0.0))
			return 0;

		final Interval x = Interval.mul(a, b);
		final Interval y = Interval.mul(c, d);

		if (0.0 < prev(x.low - y.high))
			return 1;
		if (next(x.high - y.low) < 0.0)
			return -1;

		final BigDecimal ab = new BigDecimal(a).multiply(new BigDecimal(b));
		final BigDecimal cd = new BigDecimal(c).multiply(new BigDecimal(d));
		return ab.subtract(cd).signum();
	}

	private static double next(double d) {
		if (Double.isNaN(d) || d == Double.POSITIVE_INFINITY)
			return d;

		d += 0.0d;
		return Double.longBitsToDouble(Double.doubleToRawLongBits(d) + (d >= 0.0d ? +1L : -1L));
	}

	private static double prev(final double d) {
		if (Double.isNaN(d) || d == Double.NEGATIVE_INFINITY)
			return d;

		if (d == 0.0)
			return -Double.MIN_VALUE;

		return Double.longBitsToDouble(Double.doubleToRawLongBits(d) + (d > 0.0d ? -1L : +1L));
	}
}

class Interval {
	public final double low, high;

	@Override
	public String toString() {
		final double midpoint = (high + low) / 2.0;

		if (Math.abs(midpoint) > (high - low) / 2.0)
			return new String("(" + midpoint + " ± " + (high - midpoint) + ")");

		return "[" + low + ", " + high + "]";
	}

	public Interval(final double value) {
		assert !Double.isInfinite(value) && !Double.isNaN(value);
		low = value;
		high = value;
	}

	public Interval(final double low, final double high) {
		assert !Double.isNaN(low) && !Double.isNaN(high) && low <= high;
		this.low = low;
		this.high = high;
	}

	public boolean contains(final double a) {
		return low <= a && a <= high;
	}

	public static Interval add(final Interval x, final Interval y) {
		return new Interval(prev(x.low + y.low), next(x.high + y.high));
	}

	public static Interval sub(final Interval x, final Interval y) {
		return new Interval(prev(x.low - y.high), next(x.high - y.low));
	}

	public static Interval mul(final double x, final double y) {
		assert !Double.isInfinite(x) && !Double.isNaN(x);
		assert !Double.isInfinite(y) && !Double.isNaN(y);

		if (x == 0.0 || y == 0.0)
			return new Interval(0.0, 0.0);

		final double xy = x * y;
		final double low = prev(xy);
		final double high = next(xy);

		if (xy > 0.0)
			return new Interval(0.0 >= low ? 0.0 : low, high);

		return new Interval(low, 0.0 <= high ? 0.0 : high);
	}

	public static Interval mul(final Interval x, final Interval y) {
		if (x.low == 0.0 && x.high == 0.0 || y.low == 0.0 && y.high == 0.0)
			return new Interval(0.0, -0.0);

		if (x.low >= 0.0) {
			if (y.low >= 0.0)
				return new Interval(Math.max(0.0, mulLow(x.low, y.low)), mulHigh(x.high, y.high));

			if (y.high <= 0.0)
				return new Interval(mulLow(x.high, y.low), Math.min(0.0, mulHigh(x.low, y.high)));

			return new Interval(mulLow(x.high, y.low), mulHigh(x.high, y.high));
		}

		if (x.high <= 0.0) {
			if (y.low >= 0.0)
				return new Interval(mulLow(x.low, y.high), Math.min(0.0, mulHigh(x.high, y.low)));

			if (y.high <= 0.0)
				return new Interval(Math.max(0.0, mulLow(x.high, y.high)), mulHigh(x.low, y.low));

			return new Interval(mulLow(x.low, y.high), mulHigh(x.low, y.low));
		}

		if (y.low >= 0.0)
			return new Interval(mulLow(x.low, y.high), mulHigh(x.high, y.high));

		if (y.high <= 0.0)
			return new Interval(mulLow(x.high, y.low), mulHigh(x.low, y.low));

		return new Interval(Math.min(mulLow(x.high, y.low), mulLow(x.low, y.high)), Math.max(mulHigh(x.low, y.low),
				mulHigh(x.high, y.high)));
	}

	public static double mulLow(final double x, final double y) {
		return x == 0.0 || y == 0.0 ? 0.0 : prev(x * y);
	}

	public static double mulHigh(final double x, final double y) {
		return x == 0.0 || y == 0.0 ? 0.0 : next(x * y);
	}

	private static double next(double d) {
		if (Double.isNaN(d) || d == Double.POSITIVE_INFINITY)
			return d;

		d += 0.0d;
		return Double.longBitsToDouble(Double.doubleToRawLongBits(d) + (d >= 0.0d ? +1L : -1L));
	}

	private static double prev(final double d) {
		if (Double.isNaN(d) || d == Double.NEGATIVE_INFINITY)
			return d;

		if (d == 0.0)
			return -Double.MIN_VALUE;

		return Double.longBitsToDouble(Double.doubleToRawLongBits(d) + (d > 0.0d ? -1L : +1L));
	}
}