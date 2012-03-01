package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Sensor;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Plane3;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Oranges extends World implements Sensor {
	public Oranges() {
		impulseIterations = 5;
		forceIterations = 5;

		add(this);
		surface(-4, 4);
		add(Plane3.create(Vector3.create(1, 0, 0), 8));
		add(Plane3.create(Vector3.create(-1, 0, 0), 8));
		add(Plane3.create(Vector3.create(0, 0, 1), 8));
		add(Plane3.create(Vector3.create(0, 0, -1), 8));
	}

	int frame = 0;

	@Override
	public void update() {
		frame++;
		if (frame % 250 == 0) place((Math.random() - 0.5) * 5, 40, (Math.random() - 0.5) * 5);
	}

	final Shape orange = Shape.sphere(4, 6, GLA.orange, GLA.yellow);

	void place(final double x, final double y, final double z) {
		final Body b = new Body(Vector3.create(x, y, z), Quaternion.Identity, orange, 1);
		b.sfriction = 0.5f;
		b.dfriction = 0.5f;
		b.elasticity = 0;
		add(b);
	}
}