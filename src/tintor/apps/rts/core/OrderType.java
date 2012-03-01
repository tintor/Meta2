package tintor.apps.rts.core;

import java.io.Serializable;

public abstract class OrderType implements Serializable {
	abstract void execute(World world, int player);

	public static OrderType move(final int unitID, final Vector2 destination) {
		final Action action = new Action();
		action.unitID = unitID;
		action.destination = destination;
		return action;
	}

	public static OrderType stop(final int unitID) {
		final Action order = new Action();
		order.unitID = unitID;
		order.destination = null;
		return order;
	}

	public static OrderType targetUnit(final int unitID, final int targetID) {
		final Action order = new Action();
		order.unitID = unitID;
		order.targetID = targetID;
		return order;
	}

	static class Action extends OrderType implements Serializable {
		int unitID;
		Vector2 destination;
		int targetID;

		@Override
		void execute(final World world, final int player) {
			final Unit u = world.units.get(unitID);
			if (u == null || u.player != player)
				return;
			u.destination = destination;
			u.targetId = targetID;
		}
	}
}