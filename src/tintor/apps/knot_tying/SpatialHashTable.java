package tintor.apps.knot_tying;

import tintor.geometry.Vector3;

class SpatialHashTable {
	private final float radius;
	private final int bits;
	private final int[] frames;
	private final int[] values;
	private int frame;

	public SpatialHashTable(final int bits, final float radius) {
		frames = new int[1 << bits];
		values = new int[1 << bits];
		this.bits = bits;
		this.radius = radius;
		assert test(1000);
	}

	public void clear() {
		frame++;
	}

	private static int floor(final double a) {
		return (int) Math.floor(a);
	}

	public int lookup(final Vector3 key, final int[] match) {
		int matches = 0;

		for (int x = floor(key.x - radius * 2); x <= floor(key.x + radius * 2); x++) {
			for (int y = floor(key.y - radius * 2); y <= floor(key.y + radius * 2); y++) {
				for (int z = floor(key.z - radius * 2); z <= floor(key.z + radius * 2); z++) {
					int index = hash(x, y, z);
					while (frames[index] == frame) {
						match[matches++] = values[index];
						index += 1;
						index &= (1 << bits) - 1;
					}
				}
			}
		}

		return matches;
	}

	public void add(final Vector3 key, final int value) {
		int index = hash(floor(key.x), floor(key.y), floor(key.z));
		while (frames[index] == frame) {
			index += 1;
			index &= (1 << bits) - 1;
		}
		frames[index] = frame;
		values[index] = value;
	}

	boolean test(final int n) {
		for (int x = -n; x <= n; x++) {
			for (int y = -n; y <= n; y++) {
				for (int z = -n; z <= n; z++) {
					final int i = hash(x, y, z);
					for (int a = 0; a < 8; a++) {
						assert i != hash(x + (a & 1), y + (a >> 1 & 1), z + (a >> 2 & 1)) : "x=" + x
								+ " y=" + y + " z=" + z + " a=" + a + " bits=" + bits;
					}
				}
			}
		}
		return true;
	}

	private int hash(final int x, final int y, final int z) {
		// TODO hash(x,y,z) != hash(x+-1, y+-1, z+-1)
		final int hash = (x * 769 + y) * 769 + z;
		return hash & (1 << bits) - 1 | hash >> bits;
	}
}