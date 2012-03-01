package tintor.apps.rts.test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import tintor.apps.rts.ai.SimpleAgressorAI;
import tintor.apps.rts.core.Server;
import tintor.apps.rts.core.Units;
import tintor.apps.rts.core.World;
import tintor.apps.rts.gui.PlayerGUI;

public class TestGame {
	public static void main(final String[] args) throws Exception {
		final World world = createWorld();

		//printSize(world);
		//printSize(world.units);
		//printSize(world.units.get(1));

		Server.create(world);
		PlayerGUI.create(1, "localhost");
		new SimpleAgressorAI().connect(2, "localhost");
	}

	static void printSize(final Object object) throws Exception {
		final ByteArrayOutputStream raw = new ByteArrayOutputStream();
		final ObjectOutputStream stream = new ObjectOutputStream(raw);
		stream.writeObject(object);
		stream.close();
		System.out.printf("%s size is %s bytes\n", object.getClass(), raw.toByteArray().length);
	}

	static World createWorld() {
		final World world = new World(450);
		final Units units = world.units;

		units.createMineral(-20, 10, 400);
		units.createMineral(0, -10, 200);
		units.createMineral(30, 0, 300);

		units.createBase(-200, -300, 1);
		units.createDrone(-160, -290, 1);
		units.createDrone(-160, -270, 1);

		units.createBase(200, 300, 2);
		units.createDrone(160, 290, 2).maxVelocity *= 0.5;
		units.createDrone(160, 270, 2).maxVelocity *= 0.5;

		return world;
	}
}