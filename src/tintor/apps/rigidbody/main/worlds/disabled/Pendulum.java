package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.effector.SurfaceGravity;
import tintor.apps.rigidbody.model.joint.BallJoint;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Pendulum extends World {
	public Pendulum() {
		add(new SurfaceGravity(5));
		final Shape box = Shape.sphere(2, 12);

		final float s = 2;
		for (int i = 0; i < 5; i++) {
			final Body b = new Body(Vector3.create(i * s, -5, 0), Quaternion.Identity, box, 1);
			b.elasticity = 1;
			add(b);

			add(new BallJoint(World.Space, b, Vector3.create(i * s, 5, -1)));
			add(new BallJoint(World.Space, b, Vector3.create(i * s, 5, 1)));
		}

		final Body b = new Body(Vector3.create(-10 - s, 5, 0), Quaternion.axisZ((float) (-Math.PI / 2)), box, 1);
		b.elasticity = 1;
		add(b);

		add(new BallJoint(World.Space, b, Vector3.create(-s, 5, -1)));
		add(new BallJoint(World.Space, b, Vector3.create(-s, 5, 1)));
	}
}