package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Group;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class AbsoluteElasticAngularCollision extends World {
	public AbsoluteElasticAngularCollision() {
		final Body a = new Body(Vector3.create(-2.2, 0, 0), Quaternion.Identity, Shape.box(1, 5, 1), 1);
		a.setAngVelocity(Vector3.create(0, 0, 0.1));
		a.elasticity = 1;
		add(a);
		add(Group.hingeJoint(World.Space, a, Vector3.create(-2.2, 0, 0), 1));

		final Body b = new Body(a.position().neg(), Quaternion.Identity, a.solid, 1);
		b.setAngVelocity(a.angVelocity());
		b.elasticity = a.elasticity;
		add(b);
		add(Group.hingeJoint(World.Space, b, Vector3.create(2.2, 0, 0), 1));
	}
}