package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Group;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.effector.Drag;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class SuspensionBridge extends World {
	public SuspensionBridge() {
		bridge();

		final Body b = new Body(Vector3.create(-15, 40, 0), Quaternion.Identity, Shape.sphere(2, 6, GLA.blue,
				GLA.red), 1);
		b.elasticity = 0.9f;
		b.dfriction = 0.1f;
		b.sfriction = 0.1f;
		add(b);

		add(new Drag());
		surface(-14, 4);
	}

	void bridge() {
		final Shape box = Shape.box(2.5f, 0.5f, 5);
		final Body[] b = new Body[15];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Body(Vector3.create((i - b.length * 0.5) * 3 + 1.5, 0, 0), Quaternion.Identity, box, 1);
			b[i].color = GLA.orange;
			b[i].elasticity = 1;
			add(b[i]);
		}

		for (int i = 1; i < b.length; i++)
			add(Group.hingeJoint(b[i - 1], b[i], Vector3.create(b[i].position().x - 1.5, 0, 0), 2.5f));

		add(Group.hingeJoint(World.Space, b[0], Vector3.create(b[0].position().x - 1.5, 0, 0), 2.5f));
		add(Group.hingeJoint(b[b.length - 1], World.Space, Vector3.create(b[b.length - 1].position().x + 1.5, 0, 0), 2.5f));
	}
}