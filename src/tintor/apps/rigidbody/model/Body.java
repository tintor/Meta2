package tintor.apps.rigidbody.model;

import java.util.concurrent.atomic.AtomicInteger;

import tintor.apps.rigidbody.model.solid.Solid;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Quaternion;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;

public final class Body {
	public static enum State {
		Fixed, // can't move at all
		Static, Dynamic;
	}

	// constants
	public static boolean AutoSleep = true;
	public static int SleepIdleFrames = 20;
	public static float SleepMaxVelocitySquared = 1e-5f;

	/** mass of body [kg] */
	public final float mass;
	/** inverted mass of body [1/kg] */
	public final float imass;

	private final Matrix3 Ibody;
	private Matrix3 I;
	private Matrix3 invI;
	private Transform3 transform;

	// Misc fields
	public final Solid solid;
	public State state = State.Dynamic;
	public String name;
	public int idleFrames;

	// State variables
	private Vector3 linPos = Vector3.Zero; // position
	private Quaternion angPos = Quaternion.Identity;
	private Vector3 linVel = Vector3.Zero, angVel = Vector3.Zero;
	private Vector3 biasLinVel = Vector3.Zero, biasAngVel = Vector3.Zero;

	// Externally computed quantities
	private Vector3 force = Vector3.Zero, torque = Vector3.Zero;

	// Constructor
	public Body(final Vector3 position, final Quaternion orientation, final Solid solid) {
		if (solid == null) throw new NullPointerException("solid");

		// shape
		this.solid = solid;

		if (position == null && orientation == null) {
			transform = Transform3.Identity;
			state = State.Fixed;
			mass = Float.POSITIVE_INFINITY;
			imass = 0;
			I = Ibody = Matrix3.diagonal(Float.POSITIVE_INFINITY);
			invI = Matrix3.Zero;
			return;
		}

		if (!position.isFinite()) throw new IllegalArgumentException("position");
		if (!orientation.isFinite()) throw new IllegalArgumentException("orientation");

		// position
		linPos = position;
		angPos = orientation;
		transform = Transform3.create(orientation, position);

		// init mass
		mass = solid.mass();
		imass = 1 / mass;

		// init body inertial moment
		Ibody = solid.inertiaTensor();
		I = transform.rotation.mul(Ibody).mul(transform.rotation.transpose());
		invI = I.inv();

		assert invariant();
	}

	private boolean invariant() {
		assert linPos.isFinite() : linPos;
		assert angPos.isFinite();

		assert linVel.isFinite();
		assert angVel.isFinite();

		assert force.isFinite();
		assert torque.isFinite();

		assert invI.a.isFinite() && invI.b.isFinite() && invI.c.isFinite();

		final Matrix3 m = transform.rotation;
		assert Math.abs(m.a.dot(m.b)) <= 1e-4f : Math.abs(m.a.dot(m.b));
		assert Math.abs(m.a.dot(m.c)) <= 1e-4f : Math.abs(m.a.dot(m.c));
		assert Math.abs(m.b.dot(m.c)) <= 1e-4f : Math.abs(m.b.dot(m.c));

		assert Math.abs(m.a.length() - 1) <= 1e-2 : Math.abs(m.a.length() - 1);
		assert Math.abs(m.b.length() - 1) <= 1e-2 : Math.abs(m.b.length() - 1);
		assert Math.abs(m.c.length() - 1) <= 1e-2 : Math.abs(m.c.length() - 1);
		assert Math.abs(m.colX().length() - 1) <= 1e-2 : Math.abs(m.colX().length() - 1);
		assert Math.abs(m.colY().length() - 1) <= 1e-2 : Math.abs(m.colY().length() - 1);
		assert Math.abs(m.colZ().length() - 1) <= 1e-2 : Math.abs(m.colZ().length() - 1);

		return true;
	}

	public float kinetic() {
		return (mass * linVel.square() + angVel.dot(I.mul(angVel))) / 2;
	}

	public void integrateVel(final float dt) {
		if (state == State.Dynamic) {
			// integrate
			linVel = linVel.add(dt * imass, force);
			angVel = angVel.add(dt, invI.mul(torque.sub(angVel.cross(I.mul(angVel)))));
		}

		// reset accumulators
		force = torque = Vector3.Zero;

		assert invariant();
	}

	public void integratePos(final float dt) {
		assert invariant();

		if (state == State.Dynamic) {
			// integrate
			linPos = linPos.add(dt, linVel.add(biasLinVel));
			// dq/dt = w*q/2  =>  q' = q + (w*q)*(dt/2)
			angPos = angPos.add(angVel.add(biasAngVel).mul(dt / 2).mul(angPos)).unit();

			// reset bias velocities
			biasLinVel = biasAngVel = Vector3.Zero;

			// update matrices
			transform = Transform3.create(angPos, linPos);
			I = transform.rotation.mul(Ibody).mul(transform.rotation.transpose());
			invI = I.inv();

			// auto sleep
			if (AutoSleep && linVel.square() <= SleepMaxVelocitySquared
					&& angVel.square() * solid.sphereRadius * solid.sphereRadius <= SleepMaxVelocitySquared) {
				idleFrames++;
				if (idleFrames >= SleepIdleFrames) state = State.Static;
			} else
				idleFrames = 0;
		}

		assert invariant();
	}

	public void advanceTransforms(final float dt) {
		transform = Transform3.create(angPos.add(angVel.mul(angPos).mul(dt / 2)), linPos.add(dt, linVel));
	}

	public void addForce(final Vector3 f) {
		assert force.isFinite();
		force = force.add(f);
		assert invariant();
	}

	/** force is transfered from B to A
	 *  ra and rb = point - body.position */
	public static void transferForce(final Vector3 force, final Body bodyA, final Body bodyB, final Vector3 ra,
			final Vector3 rb) {
		assert force.isFinite();
		assert ra.cross(force).isFinite() : ra + " " + force;
		assert rb.cross(force).isFinite();

		bodyA.force = bodyA.force.add(force);
		bodyA.torque = bodyA.torque.add(ra.cross(force));
		bodyB.force = bodyB.force.sub(force);
		bodyB.torque = bodyB.torque.sub(rb.cross(force));

		assert bodyA.invariant();
		assert bodyB.invariant();
	}

	public void addTorque(final Vector3 t) {
		assert torque.isFinite();
		torque = torque.add(t);
		assert invariant();
	}

	public void addLinAcc(final Vector3 acc) {
		assert acc.isFinite();
		force = force.add(mass, acc);
		assert invariant();
	}

	/** impulse is transfered from B to A
	 *  ra and rb = point - body.position */
	public static void transferImpulse(final Vector3 impulse, final Body bodyA, final Body bodyB, final Vector3 ra,
			final Vector3 rb) {
		assert impulse.isFinite();
		assert ra.cross(impulse).isFinite() : ra + " " + impulse;
		assert rb.cross(impulse).isFinite();

		bodyA.linVel = bodyA.linVel.add(bodyA.imass, impulse);
		bodyA.angVel = bodyA.angVel.add(bodyA.invI, ra.cross(impulse));
		bodyB.linVel = bodyB.linVel.sub(bodyB.imass, impulse);
		bodyB.angVel = bodyB.angVel.sub(bodyB.invI, rb.cross(impulse));

		if (bodyA.state == State.Static) bodyA.state = State.Dynamic;
		if (bodyB.state == State.Static) bodyB.state = State.Dynamic;

		assert bodyA.invariant();
		assert bodyB.invariant();
	}

	public static void transferBiasImpulse(final Vector3 impulse, final Body bodyA, final Body bodyB,
			final Vector3 ra, final Vector3 rb) {
		assert impulse.isFinite();
		assert ra.cross(impulse).isFinite() : ra + " " + impulse;
		assert rb.cross(impulse).isFinite();

		bodyA.biasLinVel = bodyA.biasLinVel.add(bodyA.imass, impulse);
		bodyA.biasAngVel = bodyA.biasAngVel.add(bodyA.invI, ra.cross(impulse));
		bodyB.biasLinVel = bodyB.biasLinVel.sub(bodyB.imass, impulse);
		bodyB.biasAngVel = bodyB.biasAngVel.sub(bodyB.invI, rb.cross(impulse));

		if (bodyA.state == State.Static) bodyA.state = State.Dynamic;
		if (bodyB.state == State.Static) bodyB.state = State.Dynamic;

		assert bodyA.invariant();
		assert bodyB.invariant();
	}

	/** @return Total absolute velocity at point relative to the center of mass. (r = point - position) */
	public Vector3 velAt(final Vector3 r) {
		// assert r.isFinite();
		return linVelocity().add(angVelocity().cross(r));
	}

	public Vector3 bVelAt(final Vector3 r) {
		assert r.isFinite();
		return biasLinVel.add(biasAngVel.cross(r));
	}

	public Matrix3 imassAt(final Vector3 r) {
		final Matrix3 rt = r.tilda();
		return Matrix3.diagonal(imass).sub(rt.mul(invI).mul(rt)); // TODO this can be simplified for special bodies
	}

	public static Matrix3 imassAt(final Body bodyA, final Body bodyB, final Vector3 ra, final Vector3 rb) {
		if (bodyA.state == Body.State.Fixed) return bodyB.imassAt(rb);
		if (bodyB.state == Body.State.Fixed) return bodyA.imassAt(ra);

		final Matrix3 rat = ra.tilda(), rbt = rb.tilda();
		final Matrix3 Ma = rat.mul(bodyA.invI).mul(rat);
		final Matrix3 Mb = rbt.mul(bodyB.invI).mul(rbt);
		return Matrix3.diagonal(bodyA.imass + bodyB.imass).sub(Ma).sub(Mb);
	}

	public Interval interval(final Vector3 axis) {
		return solid.interval(transform.iapplyVector(axis)).shift(transform.offset.dot(axis));
	}

	// Getters/Setters
	public Vector3 position() {
		return linPos;
	}

	public Quaternion orientation() {
		return angPos;
	}

	public Vector3 linVelocity() {
		return linVel;
	}

	public Vector3 angVelocity() {
		return angVel;
	}

	public void setLinVelocity(final Vector3 a) {
		linVel = a;
	}

	public void setAngVelocity(final Vector3 a) {
		angVel = a;
	}

	public Transform3 transform() {
		return transform;
	}

	public Matrix3 invI() {
		return invI;
	}

	private static final AtomicInteger lastID = new AtomicInteger();
	public int id = lastID.incrementAndGet(); // NOTE can wrap

	// From Object
	@Override
	public String toString() {
		return name != null ? name : "Body " + id;
	}
}