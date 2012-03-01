package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Sticks extends World {
	float length = 30, angle = 60;

	public Sticks() {
		final Shape s = Shape.box(length, 1, 1);

		for (int i = 0; i <= 10; i++) {
			final Body b = new Body(Vector3.create(0, 0, (i - 3) * 5), Quaternion.axisZDeg(angle), s, 1);
			b.sfriction = i * 0.1f;
			b.dfriction = 0;
			b.elasticity = 0;
			add(b);
		}

		surface(-length / 2, 5);
	}
}