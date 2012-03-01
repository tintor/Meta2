package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.ConvexPolyhedrons;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Spin extends World {
	public Spin() {
		final Body b = new Body(Vector3.create(15, 1, 0), Quaternion.Identity, Shape.box(6, 6, 6), 1);
		b.elasticity = 0.5f;
		b.sfriction = 0.5f;
		b.dfriction = 0.4f;
		b.setAngVelocity(Vector3.create(0, 1, 0));
		b.color = GLA.blue;
		b.name = "small";
		add(b);

		final Shape s = new Shape(ConvexPolyhedrons.football());
		//for (final Polygon3 p : s.faces)
		//	p.color = p.size() == 5 ? GLA.black : GLA.white;

		final Body c = new Body(Vector3.create(-15, 3, 0), Quaternion.Identity, s, 1);
		c.elasticity = 0.5f;
		c.sfriction = 0.5f;
		c.dfriction = 0.4f;
		c.setAngVelocity(Vector3.create(0, 1, 0));
		c.color = GLA.blue;
		c.name = "small";
		add(c);

		final Body a = new Body(Vector3.create(0, 3, 15), Quaternion.axisX((float) (Math.PI / 6)), new Shape(
				ConvexPolyhedrons.prism(20, 5, 5, 1)), 1);
		a.sfriction = 0.3f;
		a.dfriction = 0.3f;
		a.elasticity = 0;
		a.setAngVelocity(Vector3.create(0, 2, 0));
		add(a);

		surface(-2, 1);
	}
}