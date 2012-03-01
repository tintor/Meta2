package tintor.geometry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Immutable single-precision floating-point 3d line represented with two points.
 * 
 * @author Marko Tintor (tintor@gmail.com)
 */
public final class Line3 {
	/** Default formatter for toString() method */
	public static final InheritableThreadLocal<String> defaultFormat = new InheritableThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "Line(%s %s)";
		}
	};

	private static final AtomicInteger counter = new AtomicInteger();

	/** @return number of objects constructed */
	public static int counter() {
		return counter.get();
	}

	public static Line3 create(final Vector3 a, final Vector3 b) {
		return new Line3(a, b);
	}

	public static float distanceSquared(final Vector3 a, final Vector3 b, final Vector3 p) {
		final Vector3 d = b.sub(a);
		final float n = d.dot(p.sub(a)) / d.square();
		return (n >= 1 ? b : n <= 0 ? a : a.add(n, d)).distanceSquared(p);
	}

	private static float square(final float a) {
		return a * a;
	}

	public static float distanceSquaredInf(final Vector3 a, final Vector3 b, final Vector3 p) {
		// final Vector3 d = b.sub(a);
		// final float n = d.dot(p.sub(a)) / d.square();
		// return a.add(n, d).distanceSquared(p);

		final float dx = b.x - a.x, dy = b.y - a.y, dz = b.z - a.z;
		final float mx = p.x - a.x, my = p.y - a.y, mz = p.z - a.z;
		final float n = (dx * mx + dy * my + dz * mz) / (dx * dx + dy * dy + dz * dz);
		return square(dx * n - mx) + square(dy * n - my) + square(dz * n - mz);
	}

	// Fields
	public final Vector3 a, b;

	// Constructor
	private Line3(final Vector3 a, final Vector3 b) {
		this.a = a;
		this.b = b;
		counter.incrementAndGet();
	}

	// Operations
	public Vector3 point(final float t) {
		return Vector3.linear(a, b, t);
	}

	// With Plane
	/** Distance along the line, line is assumed infinite. */
	public float distance(final Plane3 p) {
		return -p.distance(a) / p.unitNormal.dot(b.sub(a));
	}

	// With Point
	/** @return unit vector pointing from line to point */
	public Vector3 normalInf(final Vector3 p) {
		final Vector3 d = b.sub(a), q = p.sub(a);
		return q.sub(d.dot(q) / d.square(), d).unitz();
	}

	public float nearestInf(final Vector3 p) {
		final Vector3 d = b.sub(a);
		return d.dot(p.sub(a)) / d.square();
	}

	private float nearestInfZero() {
		final Vector3 d = b.sub(a);
		return -d.dot(a) / d.square();
	}

	private static float clamp(final float a, final float min, final float max) {
		if (a > max) return max;
		if (a < min) return min;
		return a;
	}

	public float nearest(final Vector3 p) {
		return clamp(nearestInf(p), 0, 1);
	}

	public float distanceSquaredInf(final Vector3 p) {
		return distanceSquaredInf(a, b, p);
	}

	public float distanceSquared(final Vector3 p) {
		return distanceSquared(a, b, p);
	}

	// With Line
	/** @return null in case when inf lines are the same */
	public Vector3 normalInf(final Line3 q) {
		final Vector3 d = b.sub(a);
		final Vector3 n = d.unitzNormal(q.direction());
		if (n != null) return n;

		final Vector3 e = a.sub(q.a);
		return e.sub(d.dot(e) / d.square(), d).unitz();
	}

	public static class Result {
		public final float a, b;

		Result(final float a, final float b) {
			this.a = a;
			this.b = b;
		}

		boolean isFinite() {
			return Line3.isFinite(a) && Line3.isFinite(b);
		}
	}

	public Result closestInf(final Line3 q) {
		final Vector3 A = b.sub(a), B = q.a.sub(q.b), C = a.sub(q.a);
		final float aa = A.square(), bb = B.square(), ab = A.dot(B), ac = A.dot(C), bc = B.dot(C);
		final float det = aa * bb - ab * ab;
		return new Result((ab * bc - bb * ac) / det, (aa * bc - ab * ac) / det);
	}

	public Result nearestInf(final Line3 q) {
		final Result k = closestInf(q);
		return k.isFinite() ? k : new Result(nearestInfZero(), q.nearestInfZero());
	}

	public Result nearest(final Line3 q) {
		final Result k = closestInf(q);
		if (k.isFinite()) return new Result(clamp(k.a, 0, 1), clamp(k.b, 0, 1));
		final float pa = nearest(q.a), pb = nearest(q.b);
		return point(pa).distanceSquared(q.a) < point(pb).distanceSquared(q.b) ? new Result(pa, 0) : new Result(pb,
				1);
	}

	public float distanceSquaredInf(final Line3 q) {
		final Result k = nearestInf(q);
		return point(k.a).distanceSquared(q.point(k.b));
	}

	public float distanceSquared(final Line3 q) {
		final Result k = closestInf(q);
		if (k.isFinite()) return point(clamp(k.a, 0, 1)).distanceSquared(q.point(clamp(k.b, 0, 1)));
		return Math.min(distanceSquared(q.a), distanceSquared(q.b));
	}

	public float closestPointInf(final Line3 q) {
		final Vector3 A = b.sub(a), B = q.a.sub(q.b), C = a.sub(q.a);
		final float aa = A.square(), bb = B.square(), ab = A.dot(B), ac = A.dot(C), bc = B.dot(C);
		return (ab * bc - bb * ac) / (aa * bb - ab * ab);
	}

	public float nearestPointInf(final Line3 q) {
		final float x = closestPointInf(q);
		return isFinite(x) ? x : 0; // if lines are parallel every point is nearest!
	}

	public float nearestPoint(final Line3 q) {
		final float x = closestPointInf(q);
		return isFinite(x) ? clamp(x, 0, 1) : q.distanceSquared(a) < q.distanceSquared(b) ? 1 : 0;
	}

	static boolean isFinite(final float a) {
		return !Float.isInfinite(a) && !Float.isNaN(a);
	}

	// Misc
	/** @return b - a */
	public Vector3 direction() {
		return b.sub(a);
	}

	/** Returns part of line in negative side of plane */
	public Line3 clip(final Plane3 p, final float eps) {
		final float da = p.distance(a), db = p.distance(b);

		if (da > eps) {
			if (db > eps) return null;
			if (db < -eps) return Line3.create(Vector3.linear(a, b, da / (da - db)), b);
			return Line3.create(b, b);
		}
		if (da < -eps) return db > eps ? Line3.create(a, Vector3.linear(a, b, da / (da - db))) : this;
		return db > eps ? Line3.create(a, a) : this;
	}

	/** @return conversion to string using formatter */
	public final String toString(final String format) {
		return String.format(format, a, b);
	}

	@Override
	public final String toString() {
		return toString(defaultFormat.toString());
	}
}