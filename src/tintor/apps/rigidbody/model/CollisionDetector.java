package tintor.apps.rigidbody.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tintor.apps.rigidbody.model.solid.Collision;
import tintor.util.Timer;

public abstract class CollisionDetector {
	protected final List<Body> bodies = new ArrayList<Body>();
	protected final List<Contact> contacts = new ArrayList<Contact>();
	private final Collision collision = Collision.create(contacts);

	public final Timer timer = new Timer();

	public void add(final Body a) {
		bodies.add(a);
	}

	public void remove(final Body a) {
		bodies.remove(a);
	}

	public List<Body> bodies() {
		return Collections.unmodifiableList(bodies);
	}

	public List<Contact> contacts() {
		return Collections.unmodifiableList(contacts);
	}

	public void run(final boolean randomize) {
		timer.start();
		contacts.clear();
		broadPhase();
		timer.stop();

		if (randomize) Collections.shuffle(contacts);
	}

	protected abstract void broadPhase();

	protected final void narrowPhase(final Body a, final Body b) {
		if (a.state != Body.State.Dynamic && b.state != Body.State.Dynamic) return;
		collision.findContacts(a, b);
	}

	protected void bruteForce() {
		for (int a = 0; a < bodies.size(); a++)
			for (int b = a + 1; b < bodies.size(); b++)
				narrowPhase(bodies.get(a), bodies.get(b));
	}
}