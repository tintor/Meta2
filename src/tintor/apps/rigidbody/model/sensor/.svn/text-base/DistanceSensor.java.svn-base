package tintor.apps.rigidbody.model.sensor;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Sensor;
import tintor.geometry.Vector3;

public class DistanceSensor implements Sensor {
	public Body bodyA, bodyB;
	public Vector3 anchorA, anchorB;
	public double distance;

	@Override
	public void update() {
		distance = bodyA.transform().applyPoint(anchorA).distance(bodyB.transform().applyPoint(anchorB));
	}
}