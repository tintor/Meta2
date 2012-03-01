package tintor.apps.rts.test;

import java.util.Random;

import tintor.apps.rts.ai.MiningAI;
import tintor.apps.rts.core.Server;
import tintor.apps.rts.core.Unit;
import tintor.apps.rts.core.Units;
import tintor.apps.rts.core.World;

public class CooperativeMining {
	final static float WorldSize = 450;
	final static Random rand = new Random();

	public static void main(final String[] args) throws Exception {
		final World world = new World(WorldSize);
		final Units units = world.units;

		Unit.DroneMininingTicks = 5;

		for (int i = 0; i < 10; i++) {
			//units.createMineral(rand(), rand(), 200);
		}

		for (int i = 0; i < 2; i++) {
			//units.createBase(rand(), rand(), 1);
		}

		for (int i = 0; i < 5; i++) {
			//units.createDrone(rand(), rand(), 1);
		}

		Server.create(world);
		new MiningAI().connect(1, "localhost");
		//PlayerGUI.create(1, "localhost");
	}

	static float rand() {
		return (rand.nextFloat() * 2 - 1) * WorldSize;
	}
}
