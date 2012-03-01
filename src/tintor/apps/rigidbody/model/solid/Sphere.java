package tintor.apps.rigidbody.model.solid;

import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;

class Sphere extends Convex {
	final float radius;

	Sphere(final float r) {
		if (r < 0) throw new IllegalArgumentException("r = " + r);
		sphereRadius = radius = r;
	}

	@Override
	public float mass() {
		return radius * radius * radius * (float) (Math.PI * 4 / 3);
	}

	@Override
	public Matrix3 inertiaTensor() {
		return Matrix3.diagonal(mass() * radius * radius * 2 / 5);
	}

	@Override
	public float maximal(final Vector3 center) {
		return radius + (float) center.length();
	}

	@Override
	public float distance(final Vector3 point) {
		return point.square() - radius * radius;
	}

	public static int Segments = 12;

	@Override
	public void render() {
		final int as = Segments / 2, bs = Segments;
		final double ak = Math.PI / as, bk = 2 * Math.PI / bs;

		Vector3 p;
		for (int a = 1; a < as - 1; a++) {
			final double cosA = Math.cos(a * ak), cosA1 = Math.cos(a * ak + ak);
			final double sinA = Math.sin(a * ak), sinA1 = Math.sin(a * ak + ak);

			GLA.beginQuadStrip();
			for (int b = 0; b <= bs; b++) {
				final double cosB = Math.cos(b * bk), sinB = Math.sin(b * bk);

				p = Vector3.create(sinA1 * cosB, cosA1, sinA1 * sinB);
				GLA.normal(p);
				GLA.vertex(p.mul(radius));

				p = Vector3.create(sinA * cosB, cosA, sinA * sinB);
				GLA.normal(p);
				GLA.vertex(p.mul(radius));
			}
			GLA.gl.glEnd();
		}

		final double cosA1 = Math.cos(Math.PI / as), sinA1 = Math.sin(Math.PI / as);
		GLA.beginTriangleFan();
		p = Vector3.Y;
		GLA.normal(p);
		GLA.vertex(p.mul(radius));
		for (int b = bs; b >= 0; b--) {
			final double beta = b * 2 * Math.PI / bs;
			p = Vector3.create(sinA1 * Math.cos(beta), cosA1, sinA1 * Math.sin(beta));
			GLA.normal(p);
			GLA.vertex(p.mul(radius));
		}
		GLA.gl.glEnd();

		final double alpha = (as - 1) * Math.PI / as;
		final double cosA = Math.cos(alpha), sinA = Math.sin(alpha);
		GLA.beginTriangleFan();
		GLA.gl.glNormal3f(0, -1, 0);
		GLA.vertex(p.mul(radius));
		for (int b = 0; b <= bs; b++) {
			final double beta = b * 2 * Math.PI / bs;
			final double cosB = Math.cos(beta), sinB = Math.sin(beta);

			p = Vector3.create(sinA * cosB, cosA, sinA * sinB);
			GLA.normal(p);
			GLA.vertex(p.mul(radius));
		}
		GLA.gl.glEnd();
	}

	@Override
	public Interval interval(final Vector3 normal) {
		return Interval.create(-radius, radius);
	}

	//	@Override
	//	public Vector3[] intersection(final Plane3 plane) {
	//		switch (Side.classify(plane.offset - radius)) {
	//		case Positive:
	//			return new Vector3[0];
	//		case Zero:
	//			return new Vector3[] { plane.unitNormal.mul(-radius) };
	//		case Negative:
	//			final Vector3 x = plane.unitNormal.normal().unit(),
	//			y = plane.unitNormal.cross(x);
	//			// FIXME incorrect!
	//			return new Vector3[] { x.mul(radius), y.mul(radius), x.mul(-radius), y.mul(-radius) };
	//		}
	//		return null;
	//	}
}