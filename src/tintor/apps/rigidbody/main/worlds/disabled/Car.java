package tintor.apps.rigidbody.main.worlds;

import java.awt.event.KeyEvent;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Effector;
import tintor.apps.rigidbody.model.Group;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.joint.BallJoint;
import tintor.apps.rigidbody.model.joint.BarJoint;
import tintor.apps.rigidbody.model.solid.Solid;
import tintor.apps.rigidbody.tools.ConvexPolyhedrons;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Car extends World {
	final float tankX = 20, tankY = 4, tankZ = 15;
	final float wheelR = 6, wheelZ = 6, wheelDist = 15;
	final int axels = 2;

	final float ftorque = 0.65f * wheelR * wheelR, storque = ftorque;
	final float sfriction = 0.6f, dfriction = 0.5f;

	public Car() {
		impulseIterations = 10;
		forceIterations = 20;

		randomize = true;

		final Body a = ramp(-160, 0, 0, 200, 30, 100);
		final Body b = ramp(-418, 0, 180, 200, 30, 100);
		add(bridge(-283, 30, b, a));
		add(createCar());

		flipFlop();

		cone(100, -80);
		cone(100, 0);
		cone(100, 80);
		cone(170, -40);
		cone(170, 40);
		cone(240, 0);

		surface(0, 5);

		stairs();
	}

	void stairs() {
		final float stepY = 2, stepX = 30;
		for (int i = 0; i < 5; i++) {
			final Body b = new Body(Vector3.create(i * stepX, stepY / 2 + i * stepY, -200), Quaternion.Identity,
					Solid.box(200f, stepY, 100f).density(1e10f).color(GLA.brown));
			b.sfriction = World.Space.sfriction;
			b.dfriction = World.Space.dfriction;
			b.elasticity = World.Space.elasticity;
			b.state = Body.State.Fixed;
			add(b);
		}
	}

	void flipFlop() {
		final float sizeA = 100;
		final float sizeB = 100;
		final float sizeY = 20, sizeZ = 60;

		final Vector3[] desc = { Vector3.create(0, 0, 0), Vector3.create(sizeA, sizeY, 0),
				Vector3.create(-sizeB, sizeY, 0) };
		final Body b = new Body(Vector3.create(10, sizeY * 0.666, 150), Quaternion.axisY((float) Math.PI / 2),
				Solid.convex(ConvexPolyhedrons.prism(desc, desc, sizeZ)).density(0.1f).color(GLA.gray));
		b.setAngVelocity(Vector3.create(-1, 0, 0));
		b.sfriction = World.Space.sfriction;
		b.dfriction = World.Space.dfriction;
		b.elasticity = 0;
		add(b);

		add(new BallJoint(World.Space, b, b.position().add(Vector3.create(sizeZ / 2, -sizeY * 0.666, 0))));
		add(new BallJoint(World.Space, b, b.position().add(Vector3.create(-sizeZ / 2, -sizeY * 0.666, 0))));
	}

	final Solid plank = Solid.box(10, 2, 80).color(GLA.orange);

	Group bridge(final float x, final float y, final Body b1, final Body b2) {
		final Group bridge = new Group("Bridge");

		final Body[] b = new Body[10];
		for (int i = 0; i < b.length; i++) {
			b[i] = new Body(Vector3.create((i - b.length * 0.5) * 12 + x, y, 0), Quaternion.Identity, plank);
			b[i].dfriction = World.Space.dfriction;
			b[i].sfriction = World.Space.sfriction;
			b[i].elasticity = 1;
			bridge.add(b[i]);
		}

		for (int i = 1; i < b.length; i++)
			bridge.add(link(b[i - 1], b[i], (b[i - 1].position().x + b[i].position().x) / 2, y));

		bridge.add(link(b1, b[0], b[0].position().x - 6, y));
		bridge.add(link(b[b.length - 1], b2, b[b.length - 1].position().x + 6, y));

		return bridge;
	}

	static Group link(final Body a, final Body b, final float x, final float y) {
		return Group.hingeJoint(a, b, Vector3.create(x, y, 0), 50);
	}

	Solid cone = Solid.convex(ConvexPolyhedrons.pyramid(12, 40, 10)).density(1e10f).color(GLA.yellow);

	void cone(final double x, final double z) {
		final Body b = new Body(Vector3.create(x, 10.0 / 3, z), Quaternion.axisY(
				(float) (Math.random() * Math.PI * 2)).mul(Quaternion.axisXDeg(-90)), cone);
		b.dfriction = World.Space.dfriction;
		b.sfriction = World.Space.sfriction;
		b.elasticity = World.Space.elasticity;
		add(b);
	}

	Body ramp(final float x, final float z, final float a, final float sizeX, final float sizeY, final float sizeZ) {
		final Vector3[] rampDesc = { Vector3.create(0, 0, 0), Vector3.create(sizeX, 0, 0),
				Vector3.create(0, sizeY, 0) };
		final Solid ramp = Solid.convex(ConvexPolyhedrons.prism(rampDesc, rampDesc, sizeZ)).density(1e10f).color(
				GLA.brown);

		final Body b = new Body(Vector3.create(x, sizeY / 3, z), Quaternion.axisYDeg(a), ramp);
		b.dfriction = World.Space.dfriction;
		b.sfriction = World.Space.sfriction;
		b.elasticity = 0;
		b.state = Body.State.Fixed;
		add(b);
		return b;
	}

	Group createCar() {
		final Group car = new Group("Car");

		final Body s = new Body(Vector3.create(0, 1.5 * wheelR, 0), Quaternion.Identity, Solid.box(tankX, tankY,
				tankZ).density(0.5f));
		car.add(s);

		// TODO use cylinder
		final Solid wheel = Solid.convex(ConvexPolyhedrons.prism(24, wheelR, wheelR, wheelZ));
		//wheel.bicolor(Vector3.X, GLA.green, GLA.orange); TODO bicolor

		final Body[] aa = new Body[axels], bb = new Body[axels];

		for (int i = 0; i < axels; i++) {
			final double x = i * wheelDist - (axels - 1) * wheelDist * 0.5;

			final double ay = 0;//i % 2 == 0 ? wheelR / 2 : -wheelR / 2;
			final Body a = aa[i] = new Body(Vector3.create(x, s.position().y + ay, tankZ / 2 + wheelZ / 2 + 0.1),
					Quaternion.Identity, wheel);
			a.elasticity = 0.1f;
			a.sfriction = sfriction;
			a.dfriction = dfriction;
			car.add(a);
			add(Group.hingeJoint(s, a, a.position(), 1));

			final Body b = bb[i] = new Body(
					Vector3.create(x, s.position().y - ay, -tankZ / 2 - wheelZ / 2 - 0.1),
					Quaternion.Identity, wheel);
			car.add(b);
			b.elasticity = 0.1f;
			b.sfriction = sfriction;
			b.dfriction = dfriction;
			car.add(Group.hingeJoint(s, b, b.position(), 1));

			//			final Vector3 d = Vector3.create(0, wheelR, 0);
			//			joints.add(new BarJoint(aa[i], bb[i], d, d));
			//			joints.add(new BarJoint(aa[i], bb[i], d.neg(), d.neg()));

			if (i > 0) {
				Vector3 d = Vector3.create(0, wheelR, 0);
				car.add(new BarJoint(aa[i - 1], aa[i], d, d));
				car.add(new BarJoint(bb[i - 1], bb[i], d, d));

				d = Vector3.create(0, -wheelR, 0);
				car.add(new BarJoint(aa[i - 1], aa[i], d, d));
				car.add(new BarJoint(bb[i - 1], bb[i], d, d));
			}

		}

		car.add(new Effector() {
			@Override
			public void apply(final World world) {
				if (front != 0 && side != 0) return;

				final Vector3 a = aa[0].transform().applyVector(
						Vector3.create(0, 0, (front * ftorque + side * storque) * aa[0].mass));
				final Vector3 b = bb[0].transform().applyVector(
						Vector3.create(0, 0, (front * ftorque - side * storque) * bb[0].mass));
				for (int i = 0; i < axels; i++) {
					aa[i].addTorque(a);
					bb[i].addTorque(b);
				}
			}

			@Override
			public void render() {
			}

			@Override
			public String toString() {
				return "Engine";
			}
		});

		return car;
	}

	int front = 0, side = 0;

	Solid box = Solid.cube(1);

	@Override
	public void keyDown(final int key) {
		switch (key) {
		case KeyEvent.VK_UP:
			front = 1;
			break;
		case KeyEvent.VK_DOWN:
			front = -1;
			break;
		case KeyEvent.VK_LEFT:
			side = -1;
			break;
		case KeyEvent.VK_RIGHT:
			side = 1;
			break;
		case KeyEvent.VK_B:
			final Body b = new Body(Vector3.create(100 + Math.random() * 5, 40, Math.random() * 5), Quaternion
					.axisX((float) Math.random()), box.density(5).color(
					Math.random() < 0.5 ? GLA.yellow : GLA.green));
			b.elasticity = 0;
			bodies.add(b);
			break;
		}
	}

	@Override
	public void keyUp(final int key) {
		switch (key) {
		case KeyEvent.VK_UP:
			front = 0;
			break;
		case KeyEvent.VK_DOWN:
			front = 0;
			break;
		case KeyEvent.VK_LEFT:
			side = 0;
			break;
		case KeyEvent.VK_RIGHT:
			side = 0;
			break;
		}
	}
}