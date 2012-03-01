package tintor.apps.knot_tying;

import tintor.geometry.Vector3;

public class Test {
	static float link = 0.1f;
	static Rope rope = new Rope(5000, 0.095f, link);
	static Vector3 last = Vector3.Zero;

	public static void main(final String[] args) {
		rope.extend(last);
		extend(10, 0, 0);
		extend(0, 5, 0);
		extend(-5, 0, 0);
	}

	static void extend(final float dx, final float dy, final float dz) {
		final Vector3 dest = last.add(Vector3.create(dx, dy, dz));
		while (dest.distance(last) >= link) {
			last = rope.extend(dest);
		}
	}
}