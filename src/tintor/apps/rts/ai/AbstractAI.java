package tintor.apps.rts.ai;

import tintor.apps.rts.core.OrderType;
import tintor.apps.rts.core.Server;
import tintor.apps.rts.core.Unit;
import tintor.apps.rts.core.World;
import tintor.util.Host;

public abstract class AbstractAI {
	protected World world;
	protected int playerId;
	private Host host;

	protected boolean isNeutral(final Unit unit) {
		return unit.player == 0;
	}

	protected boolean isFriend(final Unit unit) {
		return unit.player == playerId;
	}

	protected boolean isEnemy(final Unit unit) {
		return !isNeutral(unit) && !isFriend(unit);
	}

	protected void control(final OrderType order) {
		host.sendObject(order);
	}

	protected abstract void run();

	public void connect(final int player, final String server) {
		final Host vhost = host = Host.connect(server, Server.Port, true);
		playerId = player;
		new Thread(this.getClass().getName() + "_" + player) {
			@Override
			public void run() {
				vhost.sendObject(player);
				while (true) {
					world = (World) vhost.receiveObject();
					AbstractAI.this.run();
				}
			}
		}.start();
	}
}