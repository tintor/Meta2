package tintor.apps.rigidbody.model.effector;

import tintor.apps.rigidbody.model.Effector;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.joint.Axis;

public class GearedMotor implements Effector {
	public Axis axis;
	public double maxForce;
	public double maxVelociy;

	@Override
	public void apply(final World world) {

	}

	@Override
	public void render() {
		// TODO Auto-generated method stub

	}
}