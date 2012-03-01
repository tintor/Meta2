package tintor.apps.rigidbody.main.worlds;

import java.util.Random;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Group;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.solid.Solid;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Wall extends World {
	public Wall() {
		final Solid brick = Solid.box(12.5f, 3.25f, 6).compile().friction(0.5f, 0.4f).elasticity(0);
		final Solid ball = Solid.sphere(15).color(GLA.gray).compile().density(5).friction(0.5f, 0.4f).elasticity(0);

		final Group wall = new Group("Wall");
		final Random r = new Random();
		for (int y = 0; y < 10; y++)
			for (int x = 0; x < 5; x++) {
				final Vector3 c = Vector3.create(1, (x % 2 == 0 ? 0.4 : 0.60) + r.nextGaussian() * 0.1, 0);
				final Body b = new Body(Vector3.create((x - 2) * 25 + (y % 2 == 0 ? 12 : 0),
						(y + 0.5) * 6.5 - 30, 0), Quaternion.Identity, brick.color(c));
				wall.add(b);
			}
		add(wall);

		final Body a = new Body(Vector3.create(0, 0, 100), Quaternion.Identity, Solid.sphere(15).density(5));
		a.name = "Ball";
		a.setLinVelocity(Vector3.create(0, 0, -60));
		a.setAngVelocity(Vector3.create(0, 0, Math.PI * 0.2));
		add(a);

		surface(-30, 5);
	}
}