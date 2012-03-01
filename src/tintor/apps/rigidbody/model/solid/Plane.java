package tintor.apps.rigidbody.model.solid;

import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Vector3;

final class Plane extends Convex {
	public final Plane3 plane;

	Plane(final Plane3 plane) {
		this.plane = plane;
		sphereRadius = Float.POSITIVE_INFINITY;
	}

	@Override
	public float mass() {
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public Matrix3 inertiaTensor() {
		return Matrix3.diagonal(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	@Override
	public float maximal(final Vector3 center) {
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public float distance(final Vector3 point) {
		return plane.distance(point);
	}

	@Override
	public void render() {
		final Vector3 c = plane.unitNormal.mul(-plane.offset);
		final Vector3 i = c.unitNormal(), j = i.unitzNormal(c);

		GLA.beginQuads();
		GLA.normal(plane.unitNormal);
		GLA.vertex(c.add(1e3f, i));
		GLA.vertex(c.add(1e3f, j));
		GLA.vertex(c.sub(1e3f, i));
		GLA.vertex(c.sub(1e3f, j));
		GLA.end();
	}

	@Override
	public Interval interval(final Vector3 dir) {
		// TODO fix it
		if (dir == plane.unitNormal) return Interval.create(Float.NEGATIVE_INFINITY, -plane.offset);
		return Interval.create(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}
}