package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Rotor extends World {
	public Rotor() {
		final Body b = new Body(Vector3.Zero, Quaternion.Identity, Shape.box(30, 4, 30), 1e3f);
		b.setAngVelocity(Vector3.create(0, 0.5, 0));
		b.elasticity = 0;
		add(b);

		final Body a = new Body(Vector3.create(-5, 3, 0), Quaternion.Identity, Shape.box(2, 2, 2), 1);
		a.color = GLA.red;
		a.elasticity = 0;
		add(a);

		final Body c = new Body(Vector3.create(0, -4, 0), Quaternion.Identity, Shape.box(100, 4, 100), 1e3f);
		c.elasticity = 0;
		c.dfriction = 0;
		c.sfriction = 0;
		c.color = GLA.orange;
		add(c);

		surface(-6, 4);
	}
}