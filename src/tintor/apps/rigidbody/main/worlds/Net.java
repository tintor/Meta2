package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.effector.Drag;
import tintor.apps.rigidbody.model.joint.BallJoint;
import tintor.apps.rigidbody.model.solid.Solid;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Net extends World {
	public Net() {
		impulseIterations = 10;
		forceIterations = 20;

		final float len = 5, dist = 6.5f;

		final Solid bar = Solid.box(len / 2, len / 20, len / 20);
		final Solid box = Solid.cube(dist / 2);

		final int n = 8, nh = n / 2;
		final Body[][] bx = new Body[n][n + 1], by = new Body[n + 1][n];

		for (int x = 0; x <= n; x++)
			for (int y = 0; y <= n; y++) {
				if (x % 2 == 0 && y % 2 == 0 && x > 0 && y > 0 && x < n && y < n) {
					final Body b = new Body(Vector3.create((x - nh) * dist, 20, (y - nh) * dist),
							Quaternion.Identity, box.color(GLA.white));
					add(b);
				}

				if (x < n) {
					bx[x][y] = new Body(Vector3.create((x - nh + 0.5) * dist, 5, (y - nh) * dist),
							Quaternion.Identity, bar);
					add(bx[x][y]);
				}

				if (y < n) {
					by[x][y] = new Body(Vector3.create((x - nh) * dist, 5, (y - nh + 0.5) * dist), Quaternion
							.axisY((float) (Math.PI / 2)), bar);
					add(by[x][y]);
				}

				if (x > 0 && x < n) joint(bx[x][y], bx[x - 1][y], (x - nh) * dist, (y - nh) * dist);
				if (y > 0 && y < n) joint(by[x][y], by[x][y - 1], (x - nh) * dist, (y - nh) * dist);

				if (x < n && y < n) joint(bx[x][y], by[x][y], (x - nh) * dist, (y - nh) * dist);
				if (x > 0 && y < n) joint(bx[x - 1][y], by[x][y], (x - nh) * dist, (y - nh) * dist);
				if (x > 0 && y > 0) joint(bx[x - 1][y], by[x][y - 1], (x - nh) * dist, (y - nh) * dist);
				if (x < n && y > 0) joint(bx[x][y], by[x][y - 1], (x - nh) * dist, (y - nh) * dist);
			}

		joint(World.Space, bx[0][0], -nh * dist, -nh * dist);
		joint(World.Space, by[0][0], -nh * dist, -nh * dist);

		joint(World.Space, bx[0][8], -nh * dist, nh * dist);
		joint(World.Space, by[0][7], -nh * dist, nh * dist);

		joint(World.Space, bx[7][8], nh * dist, nh * dist);
		joint(World.Space, by[8][7], nh * dist, nh * dist);

		joint(World.Space, bx[7][0], nh * dist, -nh * dist);
		joint(World.Space, by[8][0], nh * dist, -nh * dist);

		add(new Drag());
		surface(-20, 5);
	}

	void joint(final Body a, final Body b, final double x, final double z) {
		add(new BallJoint(a, b, Vector3.create(x, 5, z)));
	}
}