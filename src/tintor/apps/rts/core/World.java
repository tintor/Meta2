package tintor.apps.rts.core;

import java.io.Serializable;

public class World implements Serializable {
	public final static int MaxPlayers = 10;

	public final float size;
	public final Units units = new Units();
	public final int[] minerals = new int[MaxPlayers];

	public int time;

	public World(final float size) {
		this.size = size;
	}

	void update() {
		time += 1;
		units.update(this);
	}

	boolean collide(final BaseUnit a, final BaseUnit b) {
		final Vector2 d = a.position.sub(b.position);
		final float ds = d.square();
		if (ds >= Util.square(a.radius + b.radius))
			return false;

		final float f = 1 - (a.radius + b.radius) / (float) Math.sqrt(ds);
		final float k = a.mass / (a.mass + b.mass);
		a.position = clamp(a.position.add((k - 1) * f, d), a.radius);
		b.position = clamp(b.position.add(k * f, d), b.radius);
		return true;
	}

	Vector2 clamp(final Vector2 a, final float r) {
		final float x = clamp(a.x, r);
		final float y = clamp(a.y, r);
		if (a.x == x && a.y == y)
			return a;
		return Vector2.create(x, y);
	}

	private float clamp(final float a, final float r) {
		if (a < -size + r)
			return -size + r;
		if (a > size - r)
			return size - r;
		return a;
	}
}