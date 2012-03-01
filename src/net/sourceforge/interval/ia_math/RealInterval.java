package net.sourceforge.interval.ia_math;

/**
 *
 * RealInterval.java <p>
 *  -- classes implementing real intervals
 *     as part of the "ia_math library" version 0.1beta1, 10/97
 *
 * <p>
 * Copyright (C) 2000 Timothy J. Hickey
 * <p>
 * License: <a href="http://interval.sourceforge.net/java/ia_math/licence.txt">zlib/png</a>
 * <p>
 * the class RealInterval represents closed intervals of real numbers
 */

public class RealInterval implements Cloneable {
	double lo, hi;

	public RealInterval(final double lo, final double hi) throws IAException {
		if (lo <= hi) {
			this.lo = lo;
			this.hi = hi;
		} else
			throw new IAException("RealInterval(x=" + lo + ",y=" + hi + "): must have x<=y");
	}

	public RealInterval(final double x) throws IAException {
		if (Double.NEGATIVE_INFINITY < x && x < Double.POSITIVE_INFINITY) {
			lo = x;
			hi = x;
		} else
			throw new IAException("RealInterval(x): must have -inf<x<inf");

	}

	/**
	 * construct the interval [-infty,infty]
	 */
	public RealInterval() {
		lo = java.lang.Double.NEGATIVE_INFINITY;
		hi = java.lang.Double.POSITIVE_INFINITY;
	}

	public double lo() {
		return lo;
	}

	public double hi() {
		return hi;
	}

	public boolean equals(final RealInterval x) {
		return lo == x.lo && hi == x.hi;
	}

	public void intersect(final RealInterval x) throws IAException {
		lo = Math.max(lo, x.lo);
		hi = Math.min(hi, x.hi);

		if (lo <= hi)
			return;
		else
			throw new IAException("this.intersect(X): intersection is empty");
	}

	public void union(final RealInterval x) throws IAException {
		lo = Math.min(lo, x.lo);
		hi = Math.max(hi, x.hi);
	}

	public boolean nonEmpty() {
		return lo <= hi;
	}

	@Override
	public String toString() {
		return toString2();
	}

	private String toString1() {
		return new String("[" + doubleToString(lo) + " , " + doubleToString(hi) + "]");
	}

	private String toString1a() {
		return new String("[" + new Double(lo).toString() + " , " + new Double(hi).toString() + "]");
	}

	private String toString2() {
		final Double midpoint = new Double((lo + hi) / 2.0);
		final String midpointString = doubleToString((lo + hi) / 2.0);
		final String hi1String = doubleToString(hi - midpoint.doubleValue());
		if (Math.abs(midpoint.doubleValue()) > (hi - lo) / 2.0)
			return new String(
			//        this.toString1() + " = " +
					"(" + midpointString + " +/- " + hi1String + ") ");
		else
			return toString1();

	}

	private String doubleToString(final double x) {
		final StringBuffer s = new StringBuffer(new Double(x).toString());
		int i = s.length();
		int j;
		for (j = 1; j < 20 - i; i++) {
			s.append(' ');
		}
		return s.toString();
	}

	@Override
	public Object clone() {
		return new RealInterval(lo, hi);
	}

	public static RealInterval emptyInterval() {
		final RealInterval z = new RealInterval(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
		return z;
	}

	public static RealInterval fullInterval() {
		final RealInterval z = new RealInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		return z;
	}

	/**
	 * a test procedure which generates a few intervals
	 * and adds and multiplies them
	 */
	public static void main(final String[] args) {

		/* create several RealInterval objects */
		final RealInterval x = new RealInterval(-3.0, -2.0);
		final RealInterval y = new RealInterval(-6.0, 7.0);
		RealInterval z = new RealInterval();
		RealInterval w = new RealInterval();

		z = IAMath.add(x, y);

		System.out.println("x = [" + x.lo + " , " + x.hi + "]");
		System.out.println("y = [" + y.lo + " , " + y.hi + "]");
		System.out.println("x+y = [" + z.lo + " , " + z.hi + "]");

		w = IAMath.mul(x, y);
		System.out.println("x*y = [" + w.lo + " , " + w.hi + "]");

	}

}
