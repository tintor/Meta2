package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.solid.Solid;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Plane3;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Balls extends World {
	public Balls() {
		impulseIterations = 1;
		forceIterations = 1;

		final Solid ball = Solid.sphere(5, 8);
		ball.bicolor(Vector3.X, GLA.yellow, GLA.blue);

		final Body b = new Body(Vector3.create(0, 10, 0), Quaternion.Identity, ball, 1);
		b.setAngVelocity(Vector3.create(0, 0, Math.PI));
		b.setLinVelocity(Vector3.create(0, 0, 0));
		b.elasticity = 0.9f;
		b.dfriction = 0.5f;
		b.sfriction = 0.5f;
		add(b);

		final Body a = new Body(Vector3.create(10, 10, 0), Quaternion.Identity, ball, 1);
		//a.angVel = Vector3.create(0, 0, Math.PI);
		a.setLinVelocity(Vector3.create(0, 0, 0));
		a.elasticity = 0.9f;
		a.dfriction = 0.5f;
		a.sfriction = 0.5f;
		add(a);

		//joints.add(new BarJoint(a, b, Vector3.create(0, 2.5, 0), Vector3.create(0, 2.5, 0)));

		surface(-10, 1);
		add(Plane3.create(Vector3.create(1, 0, 0), 30));
		add(Plane3.create(Vector3.create(-1, 0, 0), 30));
		add(Plane3.create(Vector3.create(0, 0, 1), 30));
		add(Plane3.create(Vector3.create(0, 0, -1), 30));
	}
}