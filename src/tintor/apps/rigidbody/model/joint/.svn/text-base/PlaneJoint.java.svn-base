package tintor.apps.rigidbody.model.joint;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Joint;
import tintor.geometry.Plane3;
import tintor.geometry.Vector3;

public class PlaneJoint extends Joint {
	public Vector3 anchorA;
	public Plane3 planeB;

	private Plane3 plane;
	private float inv_nK, biasVel;

	public PlaneJoint(final Body bodyA, final Body bodyB, final Vector3 anchor, final Plane3 plane) {
		super(bodyA, bodyB);
		anchorA = anchor;
		planeB = plane;
	}

	@Override
	public void prepare(final float dt) {
		ra = bodyA.transform().applyVector(anchorA);
		final Vector3 p = bodyA.transform().offset.add(ra);
		rb = p.sub(bodyB.transform().offset);
		plane = bodyB.transform().apply(planeB);
		initImpulse();

		inv_nK = 1 / plane.unitNormal.mul(Body.imassAt(bodyA, bodyB, ra, rb)).dot(plane.unitNormal);

		biasVel = -0.2f * plane.distance(p);
	}

	@Override
	public void processCollision() {
		final Vector3 vel = bodyA.velAt(ra).sub(bodyB.velAt(rb));
		final float nVel = vel.dot(plane.unitNormal);
		addImpulse(plane.unitNormal.mul((-nVel + biasVel) * inv_nK));

		// TODO bias impulse
	}
}