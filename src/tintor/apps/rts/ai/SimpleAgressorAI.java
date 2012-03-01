package tintor.apps.rts.ai;

import java.util.ArrayList;
import java.util.List;

import tintor.apps.rts.core.OrderType;
import tintor.apps.rts.core.Unit;

public class SimpleAgressorAI extends AbstractAI {
	@Override
	protected void run() {
		final List<Unit> targets = new ArrayList<Unit>();
		for (final Unit unit : world.units)
			if (isEnemy(unit) && unit.maxVelocity > 0) {
				targets.add(unit);
			}

		int target = 0;
		for (final Unit unit : world.units)
			if (isFriend(unit)) {
				control(OrderType.move(unit.id, targets.get(target).position));
				target = (target + 1) % targets.size();
			}
	}
}
