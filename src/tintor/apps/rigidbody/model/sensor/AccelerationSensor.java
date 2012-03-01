package tintor.apps.rigidbody.model.sensor;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Sensor;
import tintor.geometry.Vector3;

public class AccelerationSensor implements Sensor {
	public Body body;
	public Vector3 anchor;

	@Override
	public void update() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}