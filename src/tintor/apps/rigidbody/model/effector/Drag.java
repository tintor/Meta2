package tintor.apps.rigidbody.model.effector;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Effector;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.Body.State;

public class Drag implements Effector {
	@Override
	public void apply(final World world) {
		// TODO add auto list to world.detector for non-fixed bodies!
		for (final Body b : world.bodies)
			if (b.state == State.Dynamic) {
				//b.addForce(b.linVel.mul(b.linVel.length() * b.linDrag));
				//b.addForce(b.linVel.mul(-b.linDrag));
				//b.addTorque(b.angVel.mul(-b.angDrag));

				// FIXME drag
				final float drag = 0.01f;

				b.setLinVelocity(b.linVelocity().mul(1 - drag));
				b.setAngVelocity(b.angVelocity().mul(1 - drag * b.solid.sphereRadius / 10));
				//b.angAcc = b.angAcc.sub(b.angDrag * b.angVel.square(), b.invI().mul(b.angVel.unitz()));
			}
	}

	@Override
	public void render() {
	}

	@Override
	public String toString() {
		return "Drag";
	}
}