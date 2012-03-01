package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.Body.State;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class AbsoluteElasticCollision extends World {
	public AbsoluteElasticCollision() {
		final Body a = new Body(Vector3.create(-6, 0, 0), Quaternion.Identity, Shape.box(2, 2, 2), 1);
		a.elasticity = 1;
		a.setLinVelocity(Vector3.create(-2, 0, 0));
		a.color = GLA.red;
		add(a);

		for (int i = -1; i < 2; i++) {
			final Body x = new Body(Vector3.create(i * 4, 0, 0), Quaternion.Identity, Shape.box(2, 2, 2), 1);
			x.elasticity = 1;
			x.color = GLA.blue;
			add(x);

			final Body b = new Body(Vector3.create(i * 4 + 2, 0, 0), Quaternion.Identity, Shape.box(2, 2, 2), 1);
			b.elasticity = 1;
			b.color = GLA.red;
			add(b);
		}

		final Body c = new Body(Vector3.create(20, 0, 0), Quaternion.Identity, Shape.box(4, 4, 4), 1e9f);
		c.state = State.Fixed;
		c.elasticity = 1;
		add(c);

		final Body d = new Body(Vector3.create(-20, 0, 0), Quaternion.Identity, Shape.box(4, 4, 4), 1e9f);
		d.state = State.Fixed;
		d.elasticity = 1;
		add(d);
	}
}