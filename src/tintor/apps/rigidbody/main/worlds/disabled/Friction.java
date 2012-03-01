package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Friction extends World {
	public Friction() {
		//      A    B
		// new
		// 10 - 0.24 (0.18)
		// 20 - 0.48 (0.36)
		// 30 - 0.74 (0.58)
		// old
		// 40 - 0.87 (0.84)
		// 50 - 1.35 (1.19)
		// 60 - 1.87 (1.73)
		// 70 - 3.01 (2.73)
		// 80 - 5.75 (5.67)

		// A measured static friction
		// B calculated static friction

		final float angle = 30;
		final float df = 0.6f, sf = 0.6f;

		final Quaternion q = Quaternion.axisZDeg(-angle);

		final Body a = new Body(q.rotate(Vector3.create(-10, 2, 0)), q, Shape.box(12, 2, 4), 1);
		a.elasticity = 0;
		a.sfriction = sf;
		a.dfriction = df;
		//a.linVel = new Vector3(-v * Math.cos(angle), v * Math.sin(angle), 0);
		a.color = GLA.blue;
		add(a);

		final Body b = new Body(Vector3.Zero, q, Shape.box(400, 2, 10), 1e10f);
		b.elasticity = 0;
		b.sfriction = sf;
		b.dfriction = sf;
		b.state = Body.State.Fixed;
		b.color = GLA.orange;
		add(b);

		surface(-15, 1);
	}
}
