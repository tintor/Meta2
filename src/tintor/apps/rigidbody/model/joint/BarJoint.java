package tintor.apps.rigidbody.model.joint;

import javax.media.opengl.GL;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Joint;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;

public class BarJoint extends Joint {
	public Vector3 anchorA, anchorB; // TODO optimize for anchorX == 0!
	public float length;

	private Matrix3 invK; // constraint matrix
	private Vector3 biasVel;
	private Vector3 dir;

	public BarJoint(final Body bodyA, final Body bodyB, final Vector3 pa, final Vector3 pb) {
		super(bodyA, bodyB);
		anchorA = pa;
		anchorB = pb;
		length = (float) bodyA.transform().applyPoint(pa).distance(bodyB.transform().applyPoint(pb));
	}

	@Override
	public void prepare(final float dt) {
		ra = bodyA.transform().applyVector(anchorA);
		rb = bodyB.transform().applyVector(anchorB);
		initImpulse();

		final Vector3 pos = bodyA.transform().offset.add(ra).sub(bodyB.transform().offset).sub(rb);
		dir = pos.div(length);

		// contraint matrix
		invK = Body.imassAt(bodyA, bodyB, ra, rb).inv();

		// bias velocity
		biasVel = dir.mul(-((float) pos.length() - length) * biasFactor / dt);
		addBiasImpulse(invK.mul(biasVel));
	}

	@Override
	public void processCollision() {
		final Vector3 vel = bodyA.velAt(ra).sub(bodyB.velAt(rb));
		addImpulse(invK.mul(dir.mul(-vel.dot(dir))));

		//		final Vector3 bvel = bodyA.bVelAt(ra).sub(bodyB.bVelAt(rb));
		//		assert bvel.isFinite();
		//		addBiasImpulse(invK.mul(biasVel.sub(bvel.dot(dir), dir)));
	}

	@Override
	public void render() {
		final Vector3 a = bodyA.transform().applyPoint(anchorA);
		final Vector3 b = bodyB.transform().applyPoint(anchorB);

		GLA.gl.glBegin(GL.GL_LINES);
		GLA.color(GLA.green);

		if (bodyA != World.Space) {
			GLA.vertex(bodyA.position());
			GLA.vertex(a);
		}

		if (bodyB != World.Space) {
			GLA.vertex(b);
			GLA.vertex(bodyB.position());
		}

		GLA.color(GLA.blue);
		GLA.vertex(a);
		GLA.vertex(b);

		GLA.gl.glEnd();
	}
}