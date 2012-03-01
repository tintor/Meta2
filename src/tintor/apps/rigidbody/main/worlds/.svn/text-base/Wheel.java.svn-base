package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.Body.State;
import tintor.apps.rigidbody.model.effector.SurfaceGravity;
import tintor.apps.rigidbody.tools.ConvexPolyhedrons;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Wheel extends World {
	public Wheel() {
		final float radius = 2;
		final Body w = new Body(Vector3.create(20, -2, 0), Quaternion.Identity, new Shape(ConvexPolyhedrons.prism(
				9, radius, radius, 6)), 1);
		w.dfriction = 0.8f;
		add(w);

		add(new SurfaceGravity(4));
		final Body q = new Body(Vector3.create(0, -8, 0), Quaternion.axisZ(Math.PI / 24), Shape.box(100, 2, 100),
				1e10f);
		q.state = State.Fixed;
		q.color = GLA.orange;
		q.dfriction = 10;
		add(q);
	}
}