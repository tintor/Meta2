package tintor.apps.rigidbody.model.sensor;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Sensor;
import tintor.geometry.Vector3;

public class MaxVelocitySensor implements Sensor {
	public Body body;
	public Vector3 anchor;
	public Vector3 direction = Vector3.Zero;

	public double maxAbsVelocity = Double.NEGATIVE_INFINITY;
	public double maxDirVelocity = Double.NEGATIVE_INFINITY;

	@Override
	public void update() {
		final Vector3 vel = body.velAt(anchor);
		maxAbsVelocity = Math.max(maxAbsVelocity, vel.length());
		maxDirVelocity = Math.max(maxDirVelocity, vel.dot(direction));
	}
}