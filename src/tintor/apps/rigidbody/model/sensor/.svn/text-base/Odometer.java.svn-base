package tintor.apps.rigidbody.model.sensor;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Sensor;
import tintor.geometry.Vector3;

public class Odometer implements Sensor {
	public Body body;
	public Vector3 anchor;
	public float distance;

	private Vector3 prev;

	public void reset() {
		prev = null;
		distance = 0;
	}

	@Override
	public void update() {
		final Vector3 curr = body.transform().applyPoint(anchor);
		if (prev != null) distance += curr.distance(prev);
		prev = curr;
	}
}