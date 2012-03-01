package tintor.apps.rigidbody.main.worlds;

import java.util.Random;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.effector.SurfaceGravity;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Tower extends World {
	public Tower() {
		impulseIterations = 40;
		forceIterations = 80;

		final int n = 6, h = 30;
		final float r = 30;

		final Random rand = new Random();
		for (int y = 0; y < h; y++) {
			final float s = 1, off = 15;

			final float ir = r * 10 / (y * s + off);
			final Shape brick = Shape.box(250 / (y * s + off), 6.5f, 120 / (y * s + off));

			for (int x = 0; x < n; x++) {
				final float p = (float) (x * 2 * Math.PI / n + (y % 2 == 0 ? 0 : Math.PI / n));
				final float q = (float) (p + Math.PI / 2);
				final Body a = new Body(Vector3.create(Math.cos(p) * ir, y * 6.5, -Math.sin(p) * ir),
						Quaternion.axisY(q), brick, 1);
				a.sfriction = 0.4f;
				a.dfriction = 0.4f;
				a.elasticity = 0.1f;
				a.color = Vector3.create(1, (rand.nextInt(2) == 0 ? 0.4 : 0.60) + rand.nextGaussian() * 0.1, 0);
				add(a);
			}
		}

		final Body plate = new Body(Vector3.create(0, -5 - 6.5 / 2, 0), Quaternion.Identity, Shape
				.box(100, 10, 100), 1e10f);
		plate.color = GLA.brown;
		add(plate);

		add(new SurfaceGravity(5));

	}

	float len = 400;
	final Shape bar = Shape.box(1, 20, 1);
}