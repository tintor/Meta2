package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.collisiondetector.Off;
import tintor.apps.rigidbody.model.effector.Servo3;
import tintor.apps.rigidbody.model.effector.Thruster;
import tintor.apps.rigidbody.model.solid.Solid;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class ServoTest extends World {
	public ServoTest() {
		super(new Off());

		servo(Vector3.create(0, -5, 0), Vector3.create(0, 0, 0), Vector3.create(0, 0, 0), 1);
		servo(Vector3.create(-5, -5, 0), Vector3.create(0, -5, 0), Vector3.create(-5, 0, 0), 1);
		servo(Vector3.create(5, -5, 0), Vector3.create(0, 5, 0), Vector3.create(5, 0, 0), 1);
		servo(Vector3.create(10, -5, 0), Vector3.create(5, 5, 0), Vector3.create(10, 0, 0), 1);
		final Body a = servo(Vector3.create(-10, -5, 0), Vector3.create(0, 0, 0), Vector3.create(-10, 0, 0), 1);

		final Thruster thruster = new Thruster();
		thruster.body = a;
		thruster.force = Vector3.create(0, -0.95, 0);
		thruster.time = 100;
		add(thruster);
	}

	private Body servo(final Vector3 pos, final Vector3 velocity, final Vector3 goal, final float force) {
		final Body a = new Body(pos, Quaternion.Identity, Solid.cube(1).color(GLA.red));
		a.setLinVelocity(velocity);
		add(a);

		add(new Body(goal, Quaternion.Identity, Solid.cube(1).color(GLA.blue)));

		final Servo3 servo = new Servo3();
		servo.maxForce = force;
		servo.goalPos = goal;
		servo.body = a;
		servo.activate();
		add(servo);
		return a;
	}
}