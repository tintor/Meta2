package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.effector.Drag;
import tintor.apps.rigidbody.model.joint.BallJoint;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Chain3R extends World {
	public Chain3R() {
		final Shape box = Shape.box(2.5f, 0.5f, 0.5f);

		Body prev = World.Space;
		for (int i = 0; i < 15; i++) {
			final Body b = new Body(Vector3.create(i * 3, 10, 0), Quaternion.Identity, box, 1);
			b.dfriction = 0.1f;
			b.sfriction = 0.2f;
			add(b);
			add(new BallJoint(prev, b, b.position().sub(Vector3.create(1.5, 0, 0))));
			prev = b;
		}
		add(new Drag());
		surface(-14, 4);
	}
}