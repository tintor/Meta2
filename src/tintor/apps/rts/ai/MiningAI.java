package tintor.apps.rts.ai;

import tintor.apps.rts.core.BaseUnit;
import tintor.apps.rts.core.OrderType;
import tintor.apps.rts.core.Unit;

public class MiningAI extends AbstractAI {
	@Override
	protected void run() {
		for (final Unit unit : world.units) {
			System.out.println(unit.type + " " + unit.id);
			if (isFriend(unit) && unit.type == Unit.Type.Drone) {
				controlDrone(unit);
			}
		}
	}

	void controlDrone(final Unit unit) {
		final Unit target = world.units.get(unit.targetId);
		if (unit.minerals < Unit.DroneMineralCapacity) {
			if (target == null || target.type != Unit.Type.Mineral) {
				control(OrderType.targetUnit(unit.id, nearestMineral(unit).id));
			}
		} else {
			if (target == null || !isFriend(target) || target.type != Unit.Type.Base) {
				control(OrderType.targetUnit(unit.id, nearestBase(unit).id));
			}
		}
	}

	Unit nearestMineral(final BaseUnit unit) {
		Unit nearest = null;
		float nearestDistance = Float.POSITIVE_INFINITY;
		for (final Unit u : world.units) {
			if (u.type == Unit.Type.Mineral) {
				final float distance = u.distance(unit);
				if (distance < nearestDistance) {
					nearest = u;
					nearestDistance = distance;
				}
			}
		}
		return nearest;
	}

	Unit nearestBase(final BaseUnit unit) {
		Unit nearest = null;
		float nearestDistance = Float.POSITIVE_INFINITY;
		for (final Unit u : world.units) {
			if (isFriend(u) && u.type == Unit.Type.Base) {
				final float distance = u.distance(unit);
				if (distance < nearestDistance) {
					nearest = u;
					nearestDistance = distance;
				}
			}
		}
		return nearest;
	}
}
