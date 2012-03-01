package tintor.apps.rigidbody.model.solid;

import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;

final class Cylinder extends Convex {
	public final float radius, halfHeight;

	Cylinder(final float r, final float a) {
		radius = r;
		halfHeight = a;
		sphereRadius = (float) Math.sqrt(radius * radius + halfHeight * halfHeight);
	}

	@Override
	public float distance(final Vector3 point) {
		final double dr = Math.sqrt(point.x * point.x + point.y * point.y) - radius;
		final float dz = Math.abs(point.z) - halfHeight;

		if (dz <= 0) return Math.max((float) dr, dz);
		if (dr <= 0) return dz;
		return (float) Math.sqrt(dz * dz + dr * dr);
	}

	@Override
	public Matrix3 inertiaTensor() {
		final float m = mass();
		final float a = m * (halfHeight * halfHeight / 3 + radius * radius / 4);
		final float b = m * radius * radius / 2;
		return Matrix3.diagonal(a, a, b);
	}

	@Override
	public float mass() {
		return radius * radius * halfHeight * (float) Math.PI * 2;
	}

	@Override
	public float maximal(final Vector3 center) {
		final double ar = Math.sqrt(center.x * center.x + center.y * center.y) + radius;
		final float az = Math.abs(center.z) + halfHeight;
		return (float) Math.sqrt(ar * ar + az * az);
	}

	public final static int Segments = 12;

	@Override
	public void render() {
		final double[] cos = new double[Segments + 1];
		final double[] sin = new double[Segments + 1];

		for (int i = 0; i < Segments; i++) {
			final double a = i * Math.PI / Segments;
			cos[i] = Math.cos(a);
			sin[i] = Math.sin(a);
		}

		cos[Segments] = cos[0];
		sin[Segments] = sin[0];

		// wrapper
		GLA.beginQuadStrip();
		for (int i = 0; i <= Segments; i++) {
			GLA.normal(cos[i], sin[i], 0);
			GLA.vertex(cos[i] * radius, sin[i] * radius, halfHeight);
			GLA.vertex(cos[i] * radius, sin[i] * radius, -halfHeight);
		}
		GLA.end();

		// bases
		GLA.normal(0, 0, 1);
		GLA.beginPolygon();
		for (int i = Segments - 1; i >= 0; i--)
			GLA.vertex(cos[i] * radius, sin[i] * radius, halfHeight);
		GLA.end();

		GLA.normal(0, 0, -1);
		GLA.beginPolygon();
		for (int i = 0; i < Segments; i++)
			GLA.vertex(cos[i] * radius, sin[i] * radius, halfHeight);
		GLA.end();
	}

	@Override
	public Interval interval(final Vector3 dir) {
		final float dz = dir.z * halfHeight;
		final float d = (float) Math.sqrt(dz * dz + (dir.x * dir.x + dir.y * dir.y) * radius * radius);
		return Interval.create(-d, d);
	}
}