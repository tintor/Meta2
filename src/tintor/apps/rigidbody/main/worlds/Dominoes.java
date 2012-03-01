package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Group;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.effector.Drag;
import tintor.apps.rigidbody.model.solid.Solid;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Dominoes extends World {
	Group g = new Group("Dominoes");

	public Dominoes() {
		float x = 20, z = -8;
		final float a = 0;
		final int n = 10;
		for (int i = 0; i < n; i++) {
			dominoe(x, z, a);
			x -= 3.5;
		}
		for (int i = 0; i <= 8; i++)
			dominoe((float) (x - 8 * Math.sin(i * Math.PI / 8)), (float) (z + 8 * Math.cos(i * Math.PI / 8) + 8),
					(float) (-i * Math.PI / 8));
		z += 16;
		for (int i = 0; i < n; i++) {
			x += 3.5;
			dominoe(x, z, a);
		}
		add(g);

		final Body p = new Body(Vector3.create(24, 1.5, z), Quaternion.Identity, Solid.cube(0.25f).density(8)
				.elasticity(0.5f).color(GLA.red));
		p.setLinVelocity(Vector3.create(-5, 0, 0));
		p.name = "Impulse";
		add(p);

		add(new Drag());
		surface(-3, 2);
	}

	final Solid box = Solid.box(0.5f, 2.5f, 1.25f).elasticity(0).color(GLA.blue).friction(0.55f, 0.5f).compile();

	void dominoe(final float x, final float z, final float a) {
		final Body b = new Body(Vector3.create(x, -0.5, z), Quaternion.axisY(a), box);
		b.name = "Dominoe";
		g.add(b);
	}
}