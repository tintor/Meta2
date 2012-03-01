package tintor.apps.rts.core;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import tintor.util.Host;

public class Server {
	public final static int Port = 2000;
	public final static int FrameTime = 50;
	public final static int MaxAcceptsPerFrame = 3;

	public final World world;

	private final Host host = Host.bind(Port);
	private final Map<InetSocketAddress, Integer> players = new HashMap<InetSocketAddress, Integer>();

	private final Thread thread;

	public Server(final World world) {
		this.world = world;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						final long frameEnd = System.currentTimeMillis() + FrameTime;
						executeFrame();
						// Wait until the end of frame
						final long timeLeft = frameEnd - System.currentTimeMillis();
						if (timeLeft > 0) {
							Thread.sleep(timeLeft);
						}
					}
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, "Server");
		thread.setPriority(Thread.MAX_PRIORITY);
	}

	void executeFrame() {
		// Receive orders from all clients
		while (true) {
			final InetSocketAddress address = host.receive();
			if (address == null) {
				break;
			}

			final Integer player = players.get(address);
			if (player != null) {
				((OrderType) host.read()).execute(world, player);
			} else {
				final Integer newPlayer = (Integer) host.read();
				players.put(address, newPlayer);
				System.out.printf("Server: Player %d connected from %s\n", newPlayer, address);
			}
		}

		// Update world state
		world.update();

		// Serialize world state
		host.write(world);

		// Copy serialized world state to client output buffers
		for (final InetSocketAddress address : players.keySet()) {
			host.send(address);
		}
	}

	public static void create(final World world) {
		new Server(world).thread.start();
	}
}
