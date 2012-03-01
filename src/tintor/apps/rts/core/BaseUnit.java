package tintor.apps.rts.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class BaseUnit implements Serializable {
	public transient Vector2 position;
	public transient Vector2 destination;

	public float maxVelocity;
	public float radius;
	public float mass;

	public float distance(final BaseUnit a) {
		return a.position.sub(position).length() - a.radius - radius;
	}

	// Move to destination and stop
	void update(final World world) {
		if (destination == null)
			return;

		final Vector2 delta = destination.sub(position);
		final float distance = delta.length();

		if (distance > maxVelocity) {
			position = world.clamp(position.add(maxVelocity / distance, delta), radius);
		} else {
			position = world.clamp(destination, radius);
			destination = null;
		}
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		Util.writeVector2(out, position);
		Util.writeVector2(out, destination);
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		position = Util.readVector2(in);
		assert position != null && position.isFinite();
		destination = Util.readVector2(in);
		assert destination == null || destination.isFinite();
	}
}
