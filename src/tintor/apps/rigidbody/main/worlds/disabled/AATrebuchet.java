package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Group;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.ConvexPolyhedrons;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class AATrebuchet extends World {
	public AATrebuchet() {
		final Body arm = new Body(Vector3.create(-8, 2, 0), Quaternion.Identity, Shape.box(30, 1, 1), 1);
		add(arm);

		final Vector3[] s = { Vector3.create(-4, -5, 0), Vector3.create(4, -5, 0), Vector3.create(0, 10, 0) };
		final Shape basis = new Shape(ConvexPolyhedrons.prism(s, s, 2));

		final Body b1 = new Body(Vector3.create(0, -6, -3), Quaternion.Identity, basis, 1);
		b1.dfriction = 0.5f;
		add(b1);

		final Body b2 = new Body(Vector3.create(0, -6, 3), Quaternion.Identity, basis, 1);
		b2.dfriction = 0.5f;
		add(b2);

		add(Group.hingeJoint(arm, b1, Vector3.create(0, 2, -3), 1));
		add(Group.hingeJoint(arm, b2, Vector3.create(0, 2, 3), 1));

		surface(-12, 4);
	}
}