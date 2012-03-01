package tintor.apps.rts.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Units implements Serializable, Iterable<Unit> {
	private transient int lastID;
	private transient Map<Integer, Unit> units = new HashMap<Integer, Unit>();

	Unit create() {
		final Unit unit = new Unit(++lastID);
		units.put(unit.id, unit);
		return unit;
	}

	void destroy(final int unitId) {
		units.remove(unitId);
	}

	public void update(final World world) {
		// run units
		for (final Unit unit : units.values()) {
			unit.update(world);
		}

		// handle unit collisions 
		for (int i = 0; i < 5; i++) {
			boolean collision = false;
			for (final Unit a : units.values()) {
				for (final Unit b : units.values())
					if (a != b && world.collide(a, b)) {
						collision = true;
					}
			}
			if (!collision) {
				break;
			}
		}

		// remove dead units
		final Iterator<Entry<Integer, Unit>> it = units.entrySet().iterator();
		while (it.hasNext()) {
			if (it.next().getValue().isDead()) {
				it.remove();
			}
		}
	}

	public Unit get(final int id) {
		return units.get(id);
	}

	public Iterator<Unit> iterator() {
		return units.values().iterator();
	}

	public Unit createMineral(final float x, final float y, final int minerals) {
		final Unit unit = create();
		unit.type = Unit.Type.Mineral;
		unit.position = Vector2.create(x, y);
		assert unit.position.isFinite();
		unit.mass = 1e8f;
		unit.radius = 20;
		unit.player = 0;
		unit.maxVelocity = 0;
		unit.minerals = (short) minerals;
		return unit;
	}

	public Unit createDrone(final float x, final float y, final int player) {
		final Unit unit = create();
		unit.type = Unit.Type.Drone;
		unit.position = Vector2.create(x, y);
		assert unit.position.isFinite();
		unit.mass = 200;
		unit.radius = 10;
		unit.player = player;
		unit.maxVelocity = 8;
		return unit;
	}

	public Unit createMarine(final float x, final float y, final int player) {
		final Unit unit = create();
		unit.type = Unit.Type.Marine;
		unit.position = Vector2.create(x, y);
		assert unit.position.isFinite();
		unit.mass = 200;
		unit.radius = 15;
		unit.player = player;
		unit.maxVelocity = 10;
		return unit;
	}

	public Unit createBase(final float x, final float y, final int player) {
		final Unit unit = create();
		unit.type = Unit.Type.Base;
		unit.position = Vector2.create(x, y);
		assert unit.position.isFinite();
		unit.mass = 1e9f;
		unit.radius = 60;
		unit.player = player;
		unit.maxVelocity = 0;
		return unit;
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		out.write(units.size());
		for (final Unit unit : units.values()) {
			out.writeObject(unit);
		}
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		final int size = in.readInt();
		units = new HashMap<Integer, Unit>(size);
		for (int i = 0; i < size; i++) {
			final Unit unit = (Unit) in.readObject();
			units.put(unit.id, unit);
		}
	}
}