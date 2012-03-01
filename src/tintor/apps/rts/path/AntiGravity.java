package tintor.apps.rts.path;

import tintor.apps.rts.core.BaseUnit;
import tintor.apps.rts.core.Vector2;
import tintor.apps.rts.core.World;

public class AntiGravity {
	public static Vector2 direction(final World world, final BaseUnit unit, final Vector2 destination,
			final BaseUnit target) {
		assert unit != null && destination != null;

		final float targetRadius = target != null ? target.radius : 0;
		Vector2 g = addGravity(Vector2.zero, destination, unit.position, (targetRadius + unit.radius) * 10);

		for (final BaseUnit u : world.units) {
			if (u != unit && u != target) {
				g = addGravity(g, unit.position, u.position, u.radius + unit.radius);
			}
		}

		System.out.println(g);
		return g;
	}

	private static Vector2 addGravity(final Vector2 g, final Vector2 a, final Vector2 b, final float m) {
		final Vector2 d = a.sub(b);
		final float i = 1.0f / d.square();
		return g.add(m * i * (float) Math.sqrt(i), d);
	}
}
