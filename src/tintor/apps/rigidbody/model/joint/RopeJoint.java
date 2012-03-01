package tintor.apps.rigidbody.model.joint;

import javax.media.opengl.GL;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Joint;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Vector3;

public class RopeJoint extends Joint {
	public final Vector3 anchorA, anchorB;
	public final float length;
	public final boolean anti;

	private Vector3 normal;
	private float inv_nK;

	private final float length_sqr;
	private boolean tight = false;

	public RopeJoint(final Body bodyA, final Body bodyB, final Vector3 pa, final Vector3 pb, final float length,
			final boolean anti) {
		super(bodyA, bodyB);
		anchorA = pa;
		anchorB = pb;
		this.anti = anti;
		this.length = length;
		length_sqr = length * length;
	}

	@Override
	public void prepare(final float dt) {
		ra = bodyA.transform().applyVector(anchorA);
		rb = bodyB.transform().applyVector(anchorB);
		initImpulse();

		final Vector3 pos = bodyA.transform().offset.add(ra).sub(bodyB.transform().offset).sub(rb);
		final float quad = pos.square();
		tight = anti ? quad >= length_sqr : quad <= length_sqr;
		if (tight) {
			normal = pos.div((float) Math.sqrt(quad));
			if (!normal.isFinite()) normal = Vector3.Zero;
			inv_nK = 1 / normal.mul(Body.imassAt(bodyA, bodyB, ra, rb)).dot(normal);
		}
	}

	@Override
	public void processCollision() {
		processContact(1); // rope elasticity
	}

	@Override
	public void processContact(final float e) {
		if (!tight) return;

		final Vector3 vel = bodyA.velAt(ra).sub(bodyB.velAt(rb));
		final float nVel = vel.dot(normal);
		if (anti ? nVel < 0 : nVel > 0) addImpulse(normal.mul(-(1 + e) * nVel * inv_nK));
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

		GLA.color(tight ? GLA.red : GLA.blue);
		GLA.vertex(a);
		GLA.vertex(b);

		GLA.gl.glEnd();
	}
}