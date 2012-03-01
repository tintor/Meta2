package tintor.apps.rigidbody.model.solid;

import java.util.List;

import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;

final class Composite extends Solid {
	final Solid[] solids;

	Composite(final List<Solid> list) {
		solids = list.toArray(new Solid[list.size()]);

		// TODO sphere could be smaller
		Vector3 sum = Vector3.Zero;
		for (final Solid solid : solids)
			sum = sum.add(solid.sphereCenter);
		sphereCenter = sum.div(solids.length);
		sphereRadius = maximal(sphereCenter);
	}

	@Override
	public Vector3 centerOfMass() {
		Vector3 p = Vector3.Zero;
		float mass = 0;
		for (final Solid s : solids) {
			final float m = s.mass();
			p = p.add(m, s.centerOfMass());
			mass += m;
		}
		return p.div(mass);
	}

	@Override
	public float mass() {
		float a = 0;
		for (final Solid s : solids)
			a += s.mass();
		return a;
	}

	@Override
	public Matrix3 inertiaTensor() {
		Matrix3 m = Matrix3.Zero;
		for (final Solid s : solids)
			m = m.add(s.inertiaTensor());
		return m;
	}

	@Override
	public float distance(final Vector3 point) {
		final float dist = Float.POSITIVE_INFINITY;

		for (final Solid s : solids) {
			final float d = s.distance(point);
			if (d < dist) return d;
		}

		return dist;
	}

	@Override
	public float maximal(final Vector3 center) {
		float r = 0;
		for (final Solid s : solids)
			r = Math.max(r, s.maximal(center));
		return r;
	}

	@Override
	public void render() {
		for (final Solid s : solids)
			s.render();
	}

	@Override
	public void collide(final Collision pair) {
		final Solid b = pair.other.solid;
		if (b instanceof Composite && b.sphereRadius > sphereRadius) b.collide(pair.other);

		for (final Solid s : solids) {
			pair.solid = s;
			if (pair.sphereTest()) s.collide(pair);
		}
	}
}