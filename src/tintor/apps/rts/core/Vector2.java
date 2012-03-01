package tintor.apps.rts.core;

import java.io.Serializable;
import java.util.Random;

public class Vector2 implements Serializable {
	public static final Vector2 zero = new Vector2(0, 0);

	public final float x, y;

	public static Vector2 create(final float x, final float y) {
		return new Vector2(x, y);
	}

	public static Vector2 randomDirection(final Random rand) {
		while (true) {
			final float x = rand.nextFloat() - 0.5f;
			final float y = rand.nextFloat() - 0.5f;

			final float s = x * x + y * y;
			if (s < 0.25) {
				final float q = (float) Math.sqrt(s);

				final float xq = x / q;
				if (!isFinite(xq)) {
					continue;
				}

				final float yq = y / q;
				if (!isFinite(yq)) {
					continue;
				}

				return Vector2.create(xq, yq);
			}
		}
	}

	private static boolean isFinite(final float a) {
		return !Float.isInfinite(a) && !Float.isNaN(a);
	}

	public boolean isFinite() {
		return isFinite(x) && isFinite(y);
	}

	private Vector2(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2 add(final Vector2 a) {
		return new Vector2(x + a.x, y + a.y);
	}

	public Vector2 add(final float v, final Vector2 a) {
		return new Vector2(x + v * a.x, y + v * a.y);
	}

	public Vector2 sub(final Vector2 a) {
		return new Vector2(x - a.x, y - a.y);
	}

	public Vector2 mul(final float a) {
		return new Vector2(x * a, y * a);
	}

	// Squared distance to point
	public float distanceSquared(final Vector2 a) {
		return (x - a.x) * (x - a.x) + (y - a.y) * (y - a.y);
	}

	// Squared distance to line segment
	public float distanceSquared(final Vector2 a, final Vector2 b) {
		final float dx = b.x - a.x, dy = b.y - a.y;
		final float sx = x - a.x, sy = y - a.y;
		final float n = (dx * sx + dy * sy) / (dx * dx + dy * dy);

		if (!isFinite(n) || n <= 0)
			return sx * sx + sy * sy;
		if (n >= 1)
			return distanceSquared(b);

		final float px = sx - n * dx, py = sy - n * dy;
		return px * px + py * py;
	}

	public Vector2 unit() {
		final float d = length();
		return new Vector2(x / d, y / d);
	}

	public float dot(final Vector2 a) {
		return x * a.x + y * a.y;
	}

	public float square() {
		return dot(this);
	}

	public float length() {
		return (float) Math.sqrt(square());
	}

	@Override
	public String toString() {
		return "[" + x + " " + y + "]";
	}
}