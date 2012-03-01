package tintor.apps.rigidbody.model.effector;

import javax.media.opengl.GL;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Effector;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Vector3;

public class MuscleServo implements Effector {
	private final YServo servo = new YServo();
	private final Body bodyA, bodyB;
	private final Vector3 laAnchorA, lbAnchorB;

	public float maxForce = 1;
	public boolean active = false;

	public float goalPos;
	public float goalVel;

	public MuscleServo(final Body bodyA, final Body bodyB, final Vector3 wAnchorA, final Vector3 wAnchorB) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;

		laAnchorA = bodyA.transform().iapplyPoint(wAnchorA);
		lbAnchorB = bodyB.transform().iapplyPoint(wAnchorB);

		goalPos = (float) wAnchorA.distance(wAnchorB);
	}

	@Override
	public void apply(final World world) {
		if (goalPos < 0) goalPos = 0;

		if (!active) return;

		final Vector3 wAnchorA = bodyA.transform().applyPoint(laAnchorA);
		final Vector3 wAnchorB = bodyB.transform().applyPoint(lbAnchorB);

		final Vector3 ra = wAnchorA.sub(bodyA.position());
		final Vector3 rb = wAnchorB.sub(bodyB.position());

		final Vector3 wVelAnchorA = bodyA.velAt(ra);
		final Vector3 wVelAnchorB = bodyB.velAt(rb);

		final Vector3 d = wAnchorA.sub(wAnchorB);
		final float len = (float) d.length();
		final Vector3 dir = d.div(len);

		final float dr = len - goalPos;
		final float dv = dir.dot(wVelAnchorA, wVelAnchorB) - goalVel;
		final Vector3 force = dir.mul(servo.eval(dr, dv) * maxForce);

		Body.transferForce(force, bodyA, bodyB, ra, rb);
	}

	@Override
	public void render() {
		final Vector3 a = bodyA.transform().applyPoint(laAnchorA);
		final Vector3 b = bodyB.transform().applyPoint(lbAnchorB);

		GLA.gl.glBegin(GL.GL_LINES);
		GLA.color(GLA.yellow);
		GLA.vertex(a);
		GLA.vertex(b);
		GLA.gl.glEnd();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + System.identityHashCode(this);
	}
}
