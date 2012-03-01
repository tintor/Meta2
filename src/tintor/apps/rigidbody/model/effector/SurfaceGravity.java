package tintor.apps.rigidbody.model.effector;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Effector;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.Body.State;
import tintor.geometry.Vector3;

public class SurfaceGravity implements Effector {
	public Vector3 gravity;
	public double zero; // height with zero potential energy

	public SurfaceGravity(final double g) {
		this(Vector3.create(0, -g, 0));
	}

	public SurfaceGravity(final Vector3 gravity) {
		this.gravity = gravity;
	}

	@Override
	public void apply(final World world) {
		for (final Body b : world.bodies)
			if (b.state != State.Fixed) b.addLinAcc(gravity);
	}

	@Override
	public void render() {
	}

	@Override
	public String toString() {
		return "SurfaceGravity";
	}
}