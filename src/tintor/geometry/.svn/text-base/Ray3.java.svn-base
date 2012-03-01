package tintor.geometry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Immutable single-precision floating-point 3d ray represented with origin point and unit direction.
 * 
 * @author Marko Tintor (tintor@gmail.com)
 */
public final class Ray3 {
	// Static fields
	/** Default formatter for toString() method */
	public static final InheritableThreadLocal<String> defaultFormat = new InheritableThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "Ray(%s %s)";
		}
	};

	private static final AtomicInteger counter = new AtomicInteger();

	/** @return number of objects constructed */
	public static int counter() {
		return counter.get();
	}

	// Static factory methods
	public static Ray3 ray(final Vector3 origin, final Vector3 dir) {
		return new Ray3(origin, dir);
	}

	public static Ray3 line(final Vector3 a, final Vector3 b) {
		return new Ray3(a, b.sub(a));
	}

	/** ASSUME |dir| = 1 AND |normal| = 1 */
	public static Ray3 reflect(final Vector3 dir, final Vector3 point, final Vector3 normal) {
		return new Ray3(point, dir.sub(2 * normal.dot(dir), normal));
	}

	// Fields
	/** origin of ray */
	public final Vector3 origin;
	/** unit direction of ray */
	public final Vector3 unitDir;

	// Constructor
	private Ray3(final Vector3 origin, final Vector3 dir) {
		this.origin = origin;
		unitDir = dir.unit();
		counter.incrementAndGet();
	}

	/** @return point on the ray. */
	public Vector3 point(final float t) {
		return origin.add(t, unitDir);
	}

	// With Plane
	/**
	 * @return distance along the ray<br>
	 *         Returns +-Inf if ray is parallel to plane<br>
	 *         Returns NaN if ray is normal to plane
	 */
	public float distance(final Plane3 p) {
		return -p.distance(origin) / p.unitNormal.dot(unitDir);
	}

	// With Point
	public float nearest(final Vector3 p) {
		return unitDir.dot(p, origin);
	}

	public float distanceSquared(final Vector3 p) {
		return point(nearest(p)).distanceSquared(p);
	}

	// With Ray
	public static class Result {
		final float a, b;

		Result(final float a, final float b) {
			this.a = a;
			this.b = b;
		}

		boolean isFinite() {
			return !Float.isInfinite(a) && !Float.isNaN(a) && !Float.isInfinite(b) && !Float.isNaN(b);
		}
	}

	/**
	 * @return coordinates of closest points between this and q.<BR>
	 *         Returns non-finite Vector2 if this.dir is colinear with q.dir.
	 */
	public Result closest(final Ray3 q) {
		final Vector3 r = origin.sub(q.origin);
		final float a = unitDir.dot(q.unitDir), b = unitDir.dot(r), c = q.unitDir.dot(r), d = 1 - a * a;
		return new Result((a * c - b) / d, (a * b - c) / d);
	}

	public Result nearest(final Ray3 q) {
		final Result k = closest(q);
		return k.isFinite() ? k : new Result(-origin.dot(unitDir), -q.origin.dot(q.unitDir));
	}

	public float distanceSquared(final Ray3 q) {
		final Result k = nearest(q);
		return point(k.a).distanceSquared(point(k.b));
	}

	/** @return conversion to string using formatter */
	public final String toString(final String format) {
		return String.format(format, origin, unitDir);
	}

	@Override
	public final String toString() {
		return toString(defaultFormat.toString());
	}
}