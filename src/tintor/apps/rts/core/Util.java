package tintor.apps.rts.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class Util {
	static float square(final float a) {
		return a * a;
	}

	static void writeVector2(final ObjectOutputStream out, final Vector2 a) throws IOException {
		if (a == null) {
			out.writeFloat(Float.NaN);
			return;
		}

		assert !Double.isNaN(a.x) && !Double.isNaN(a.y);
		out.writeFloat(a.x);
		out.writeFloat(a.y);
	}

	static Vector2 readVector2(final ObjectInputStream in) throws IOException {
		final float x = in.readFloat();
		return Float.isNaN(x) ? null : Vector2.create(x, in.readFloat());
	}
}