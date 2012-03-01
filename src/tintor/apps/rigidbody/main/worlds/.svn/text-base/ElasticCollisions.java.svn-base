package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class ElasticCollisions extends World {
	public ElasticCollisions() {
		final Shape box = Shape.box(1.5f, 1.5f, 1.5f);
		final int n = 10;
		for (int i = 0; i < n; i++) {
			final Body a = new Body(Vector3.create(0 + (i - (n - 1) * 0.5) * 4, 10, 0), Quaternion.Identity, box,
					1e3f);
			a.elasticity = (float) (1 - Math.pow(2, -i));
			a.sfriction = 0;
			a.dfriction = 0;
			a.color = GLA.blue;
			add(a);
		}

		surface(-13, 1);
	}
}