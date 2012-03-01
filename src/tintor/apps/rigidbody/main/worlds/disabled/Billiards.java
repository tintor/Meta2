package tintor.apps.rigidbody.main.worlds;

import java.awt.event.KeyEvent;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Shape;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Billiards extends World {
	Body que;

	public Billiards() {
		table();

		que = ball(0, 12, GLA.white, GLA.white);

		for (int j = 1; j <= 4; j++)
			for (int i = 1; i <= j; i++)
				ball((i - 1) * 2 - (j - 1), -j * 2 - 7, GLA.yellow, GLA.white);

		surface(-1, 4);
	}

	Body ball(final double x, final double z, final Vector3 c1, final Vector3 c2) {
		final Body b = new Body(Vector3.create(x, 0, z), Quaternion.Identity, Shape.sphere(2, 6, c1, c2), 1);
		b.sfriction = 0.1f;
		b.dfriction = 0.1f;
		b.elasticity = 1;
		add(b);
		return b;
	}

	void table() {
		final float hole = 3, seg = 20, w = 2;
		final Shape s = Shape.box(w, 2, seg);

		part(Vector3.create(-seg / 2 - w, 0, seg / 2 + hole / 2), s, 0);
		part(Vector3.create(-seg / 2 - w, 0, -seg / 2 - hole / 2), s, 0);
		part(Vector3.create(seg / 2 + w, 0, seg / 2 + hole / 2), s, 0);
		part(Vector3.create(seg / 2 + w, 0, -seg / 2 - hole / 2), s, 0);

		part(Vector3.create(0, 0, -seg - hole - w), s, (float) (Math.PI / 2));
		part(Vector3.create(0, 0, seg + hole + w), s, (float) (Math.PI / 2));

		//		final Body b = new Body(Vector3.create(0, -2, 0), Quaternion.Identity, Shape.box(seg + hole, 2,
		//				(seg + hole + w) * 2), 1e10);
		//		b.state = Body.State.Fixed;
		//		b.color = Vector3.create(0, 0.5, 0);
		//		add(b);
	}

	void part(final Vector3 pos, final Shape shape, final float angle) {
		final Body b = new Body(pos, Quaternion.axisY(angle), shape, 1e10f);
		b.state = Body.State.Fixed;
		b.color = GLA.brown;
		b.dfriction = World.Space.dfriction;
		b.sfriction = World.Space.sfriction;
		b.elasticity = World.Space.elasticity;
		add(b);
	}

	void hit(final double x, final double z) {
		que.setLinVelocity(que.linVelocity().add(Vector3.create(x, 0, z)));
		que.state = Body.State.Dynamic;
	}

	@Override
	public void keyDown(final int key) {
		switch (key) {
		case KeyEvent.VK_UP:
			final double a = camera.yaw * Math.PI / 180;
			hit(-10 * Math.sin(a), -10 * Math.cos(a));
			break;
		}
	}
}