package tintor.apps.rigidbody.model.solid;

import java.util.Arrays;

import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;

final class Box extends Convex {
	public final float x, y, z;
	public Polyhedron polyhedron;
	final Vector3[] vertices;

	Box(final float a, final float b, final float c) {
		if (a <= 0 || b <= 0 || c <= 0) throw new IllegalArgumentException();

		x = a;
		y = b;
		z = c;

		vertices = new Vector3[] { Vector3.create(a, b, c), Vector3.create(a, b, -c), Vector3.create(a, -b, c),
				Vector3.create(a, -b, -c), Vector3.create(-a, b, c), Vector3.create(-a, b, -c),
				Vector3.create(-a, -b, c), Vector3.create(-a, -b, -c) };

		sphereRadius = (float) Math.sqrt(x * x + y * y + z * z);

		polyhedron = new Polyhedron(Arrays.asList(vertices));
	}

	@Override
	public float mass() {
		return x * y * z * 8;
	}

	@Override
	public Matrix3 inertiaTensor() {
		final float k = mass() / 3;
		final float a = y * y + z * z;
		final float b = x * x + z * z;
		final float c = x * x + y * y;
		return Matrix3.diagonal(a * k, b * k, c * k);
	}

	@Override
	public float maximal(final Vector3 center) {
		final float ax = Math.abs(center.x) + x;
		final float ay = Math.abs(center.y) + y;
		final float az = Math.abs(center.z) + z;
		return (float) Math.sqrt(ax * ax + ay * ay + az * az);
	}

	@Override
	public float distance(final Vector3 point) {
		final float dx = Math.abs(point.x) - x;
		final float dy = Math.abs(point.y) - y;
		final float dz = Math.abs(point.z) - z;

		float m = dx;
		if (dy > m) m = dy;
		if (dz > m) m = dz;
		if (m <= 0) return m;

		float s = 0;
		if (dx > 0) s += dx * dx;
		if (dy > 0) s += dy * dy;
		if (dz > 0) s += dz * dz;
		return (float) Math.sqrt(s);
	}

	@Override
	public void render() {
		GLA.beginQuads();

		// front
		GLA.normal(0, 0, 1);
		GLA.vertex(-x, -y, z);
		GLA.vertex(x, -y, z);
		GLA.vertex(x, y, z);
		GLA.vertex(-x, y, z);

		// back
		GLA.normal(0, 0, -1);
		GLA.vertex(-x, y, -z);
		GLA.vertex(x, y, -z);
		GLA.vertex(x, -y, -z);
		GLA.vertex(-x, -y, -z);

		// left
		GLA.normal(-1, 0, 0);
		GLA.vertex(-x, -y, z);
		GLA.vertex(-x, y, z);
		GLA.vertex(-x, y, -z);
		GLA.vertex(-x, -y, -z);

		// right
		GLA.normal(1, 0, 0);
		GLA.vertex(x, -y, -z);
		GLA.vertex(x, y, -z);
		GLA.vertex(x, y, z);
		GLA.vertex(x, -y, z);

		// top
		GLA.normal(0, 1, 0);
		GLA.vertex(-x, y, z);
		GLA.vertex(x, y, z);
		GLA.vertex(x, y, -z);
		GLA.vertex(-x, y, -z);

		// bottom
		GLA.normal(0, -1, 0);
		GLA.vertex(-x, -y, -z);
		GLA.vertex(x, -y, -z);
		GLA.vertex(x, -y, z);
		GLA.vertex(-x, -y, z);

		GLA.end();
	}

	@Override
	public Interval interval(final Vector3 dir) {
		final float m = Math.abs(dir.x) * x + Math.abs(dir.y) * y + Math.abs(dir.z) * z;
		return Interval.create(-m, m);
	}
}