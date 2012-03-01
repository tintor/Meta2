package tintor.apps.rigidbody.model.effector;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Effector;
import tintor.apps.rigidbody.model.World;
import tintor.geometry.Vector3;

public class Servo1R implements Effector {
	private final YServo servo = new YServo();
	private final Body bodyA, bodyB;
	private final Vector3 laAnchor, lbAnchor;
	private final Vector3 laRefA, lbRefB;

	public float maxTorque = 1;
	public boolean active = false;

	public float goalPos = (float) Math.PI / 2;
	public float goalVel = 0;

	public Servo1R(final Body bodyA, final Body bodyB, final Vector3 wAnchor, final Vector3 wRefA, final Vector3 wRefB) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;

		laAnchor = bodyA.transform().iapplyPoint(wAnchor);
		lbAnchor = bodyB.transform().iapplyPoint(wAnchor);

		laRefA = reference(laAnchor, bodyA.transform().iapplyPoint(wRefA));
		lbRefB = reference(lbAnchor, bodyB.transform().iapplyPoint(wRefB));
	}

	private static Vector3 reference(final Vector3 a, final Vector3 b) {
		return a.add(b.sub(a).unit());
	}

	@Override
	public void apply(final World world) {
		if (!active) return;

		final Vector3 wRefA = bodyA.transform().applyPoint(laRefA);
		final Vector3 wRefB = bodyB.transform().applyPoint(lbRefB);
		final Vector3 wAnchor = bodyA.transform().applyPoint(laAnchor).add(bodyB.transform().applyPoint(lbAnchor))
				.mul(0.5f);

		final float dr = (float) Math.acos(wRefA.sub(wAnchor).dot(wRefB.sub(wAnchor)));
		final float dv = 0;

		final float torque = servo.eval(dr, dv) * maxTorque;

		// TODO compute torques in world space 
		bodyA.addTorque(null);
		bodyB.addTorque(null);
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
