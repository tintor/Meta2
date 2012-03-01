package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Effector;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.Body.State;
import tintor.apps.rigidbody.model.joint.BallJoint;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Train extends World {
	public Train() {
		final Shape rail = Shape.box(100, 0.5f, 0.5f);
		final Shape wagon = Shape.box(10, 3, 3); // 90m^2

		Body prev = null;
		for (int i = 0; i < 10; i++) {
			final Body b = new Body(Vector3.create(i * 11, 0, 0), Quaternion.Identity, wagon, 1);
			b.dfriction = 0.1f;
			b.sfriction = 0.15f;
			b.color = i > 0 ? GLA.blue : GLA.red;
			add(b);

			if (i > 0) {
				add(new BallJoint(prev, b, Vector3.create(i * 11 - 5.5, -1, 0)));
				add(new BallJoint(prev, b, Vector3.create(i * 11 - 5.5, 1, 0)));
			}
			prev = b;

			if (i == 0) add(new Effector() {
				@Override
				public void apply(final World world) {
					b.addForce(b.orientation().dirX().neg().mul(600));
				}

				@Override
				public void render() {
				}
			});
		}

		final Body r = new Body(Vector3.create(-35, -1.5 + 0.25, -5), Quaternion.axisY(Math.PI / 12), rail, 1e20f);
		r.state = State.Fixed;
		r.color = GLA.gray;
		add(r);

		surface(-1.5f, 5);
	}
}