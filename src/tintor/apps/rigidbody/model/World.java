package tintor.apps.rigidbody.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tintor.apps.rigidbody.model.collisiondetector.BruteForce;
import tintor.apps.rigidbody.model.effector.SurfaceGravity;
import tintor.apps.rigidbody.model.solid.Solid;
import tintor.apps.rigidbody.tools.GLA;
import tintor.apps.rigidbody.view.OrbitingCamera;
import tintor.geometry.Plane3;
import tintor.geometry.Vector3;
import tintor.util.Timer;

public class World {
	public static final Body Space = new Body(null, null, Solid.Point);

	// Parameters
	public float timeStep = 0.01f;
	public boolean randomize = false;
	public boolean showTiming = false;

	public int impulseIterations = 40;
	public int forceIterations = 80;

	// Model
	public float time = 0;
	public Body pickBody;
	public OrbitingCamera camera = new OrbitingCamera();

	// Components
	private final List<Constraint> joints = new ArrayList<Constraint>();
	private final List<Effector> effectors = new ArrayList<Effector>();
	private final List<Sensor> sensors = new ArrayList<Sensor>();

	private final CollisionDetector detector;
	public final List<Body> bodies;
	public final List<Contact> contacts;

	public World() {
		this(new BruteForce());
	}

	public World(final CollisionDetector detector) {
		this.detector = detector;
		bodies = detector.bodies();
		contacts = detector.contacts();
	}

	// Operations
	public void step(final int n) {
		timer.reset();
		detector.timer.reset();

		for (int i = 0; i < n; i++)
			step();

		if (showTiming) System.out.printf("physics: %s, detector: %s\n", timer, detector.timer);
		detector.run(randomize);
	}

	public Timer timer = new Timer();

	private void step() {
		// advance transforms
		//for (final Body b : detector.bodies)
		//	b.advanceTransforms(timeStep);

		// collision detection
		detector.run(randomize);

		// randomization
		if (randomize) Collections.shuffle(joints);

		timer.start();

		// prepare constraints
		prepare();

		// process collisions
		for (int i = 0; i < impulseIterations; i++)
			processCollisions();

		// calculate external forces/torques for each body
		for (final Effector m : effectors)
			m.apply(this);

		// integrate velocities
		for (final Body b : detector.bodies)
			b.integrateVel(timeStep);

		// process contacts
		final float k = 1.0f / forceIterations;
		for (int i = 1; i <= forceIterations; i++)
			processContacts(i * k - 1); // e is lineary interpolated from -1+e
		// to 0
		// processContacts(0); // e is lineary interpolated from -1+e to 0

		// correct positions
		correct();

		// integrate positions
		for (final Body b : detector.bodies)
			b.integratePos(timeStep);

		// update time
		time += timeStep;

		timer.stop();

		// update sensors
		for (final Sensor s : sensors)
			s.update();
	}

	private void prepare() {
		for (final Constraint c : joints)
			c.prepare(timeStep);
		for (final Constraint c : contacts)
			c.prepare(timeStep);
	}

	private void processCollisions() {
		for (final Constraint c : joints)
			c.processCollision();
		for (final Constraint c : contacts)
			c.processCollision();
	}

	private void processContacts(final float e) {
		for (final Constraint c : joints)
			c.processContact(e);
		for (final Constraint c : contacts)
			c.processContact(e);
	}

	private void correct() {
		for (final Constraint c : joints)
			c.correct(timeStep);
		for (final Constraint c : contacts)
			c.correct(timeStep);
	}

	public Body bodyByName(final String name) {
		for (final Body b : bodies)
			if (name.equals(b.name)) return b;
		throw new RuntimeException();
	}

	public void keyDown(@SuppressWarnings("unused") final int key) {
	}

	public void keyUp(@SuppressWarnings("unused") final int key) {
	}

	// Adding/Removing objects
	private final List<Object> list = new ArrayList<Object>();

	public void add(final Group g) {
		list.add(g);
		addGroup(g);
	}

	private void addGroup(final Group g) {
		for (final Object o : g.list)
			if (o instanceof Body)
				detector.add((Body) o);
			else if (o instanceof Joint)
				joints.add((Joint) o);
			else if (o instanceof Effector)
				effectors.add((Effector) o);
			else if (o instanceof Sensor)
				sensors.add((Sensor) o);
			else if (o instanceof Group)
				addGroup((Group) o);
			else
				throw new RuntimeException();
	}

	public void add(final Body b) {
		list.add(b);
		detector.add(b);
	}

	public void remove(final Body b) {
		if (list.remove(b)) detector.remove(b);
	}

	public void add(final Joint joint) {
		list.add(joint);
		joints.add(joint);
	}

	public void add(final Sensor sensor) {
		list.add(sensor);
		sensors.add(sensor);
	}

	public void add(final Effector effector) {
		list.add(effector);
		effectors.add(effector);
	}

	public void populate(final List<Object> outList) {
		outList.addAll(list);
	}

	public void surface(final float height, final float gravity) {
		add(new SurfaceGravity(gravity));
		final Body b = new Body(null, null, Solid.plane(Plane3.create(Vector3.Y, -height)).color(GLA.gray));
		b.name = "Surface";
		add(b);
	}

	// Rendering
	public void renderJoints() {
		for (final Constraint c : joints)
			c.render();
	}

	public void renderEffectors() {
		for (final Effector e : effectors)
			e.render();
	}
}