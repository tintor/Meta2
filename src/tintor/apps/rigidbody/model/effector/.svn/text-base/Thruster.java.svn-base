package tintor.apps.rigidbody.model.effector;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Effector;
import tintor.apps.rigidbody.model.World;
import tintor.geometry.Vector3;

public class Thruster implements Effector {
	public Body body;
	public Vector3 force;
	public float time;

	@Override
	public void apply(final World world) {
		if (time <= 0) return;
		body.addForce(time < world.timeStep ? force.mul(time / world.timeStep) : force);
		time -= world.timeStep;
	}
	
	@Override
	public void render() {
	}
}