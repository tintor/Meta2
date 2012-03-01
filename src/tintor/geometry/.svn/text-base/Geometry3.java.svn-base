package tintor.geometry;

import tintor.util.Visitor;

/**
 * Various geometric operations.
 * 
 * @author Marko Tintor (tintor@gmail.com)
 */
public abstract class Geometry3 {
	/** @return intersection point of three planes */
	public static Vector3 intersection(final Plane3 a, final Plane3 b, final Plane3 c) {
		final Vector3 ab = a.unitNormal.cross(b.unitNormal);
		final Vector3 bc = b.unitNormal.cross(c.unitNormal);
		final Vector3 ca = c.unitNormal.cross(a.unitNormal);
		return ab.mul(c.offset).add(a.offset, bc).add(b.offset, ca).div(-a.unitNormal.dot(bc));
	}

	/** @return intersection ray of two planes */
	public static Ray3 intersection(final Plane3 a, final Plane3 b) {
		final Vector3 dir = a.unitNormal.cross(b.unitNormal);
		final Vector3 origin = solveLinearRowSpec(dir, a.unitNormal, b.unitNormal, -a.offset, -b.offset);
		return origin.isFinite() ? Ray3.ray(origin, dir) : null;
	}

	/** @return R such that (A*R, B*R, C*R) = (0, dy, dz) */
	private static Vector3 solveLinearRowSpec(final Vector3 a, final Vector3 b, final Vector3 c, final float dy,
			final float dz) {
		final float p = b.z * dz - dy * c.z, q = b.y * dz - dy * c.y, r = dy * c.x - b.x * dz;
		return Vector3.div(a.y * p - a.z * q, a.x * p + a.z * r, a.x * q + a.y * r, a.mixed(b, c));
	}

	/** @return R such that (A*R, B*R, C*R) = D */
	public static Vector3 solveLinearRow(final Vector3 a, final Vector3 b, final Vector3 c, final Vector3 d) {
		final float p = b.z * d.z - d.y * c.z, q = d.y * c.y - b.y * d.z, r = d.y * c.x - b.x * d.z;
		final float x = a.y * p - a.z * q + d.x * (b.y * c.z - b.z * c.y);
		final float y = a.x * p + a.z * r + d.x * (b.x * c.z - b.z * c.x);
		final float z = a.x * q + a.y * r + d.x * (b.x * c.y - b.y * c.x);
		return Vector3.div(x, y, z, a.mixed(b, c));
	}

	/** @return Surface of parallelogram with vertices: 0, a, b, a+b */
	public static double parallelogramSurface(final Vector3 a, final Vector3 b) {
		final double ss = a.dot(b);
		return Math.sqrt(a.square() * b.square() - ss * ss);
	}

	public static Line3 convexClip(final Polygon3[] faces, final Line3 line, final float eps) {
		return convexClip(faces, line.a, line.b, eps);
	}

	public static Line3 convexClip(final Polygon3[] faces, Vector3 a, Vector3 b, final float eps) {
		for (final Polygon3 face : faces) {
			final float da = face.plane.distance(a);
			final float db = face.plane.distance(b);

			if (da > eps) {
				if (db > eps) return null;
				a = db < -eps ? Vector3.linear(a, b, da / (da - db)) : b;
			} else if (db > eps) b = da < -eps ? Vector3.linear(a, b, da / (da - db)) : a;
		}

		return Line3.create(a, b);
	}

	public static Visitor<Vector3, Float> signedVolume() {
		return new Visitor<Vector3, Float>() {
			float volume;
			float s, z;
			int n;
			Vector3 last, first;

			@Override
			public void begin() {
				s = z = 0;
				n = 0;
				last = null;
			}

			@Override
			public void visit(final Vector3 b) {
				if (last != null)
					addEdge(last, b);
				else
					first = b;
				last = b;
			}

			@Override
			public void end() {
				addEdge(last, first);
				volume += z * s / n;
			}

			void addEdge(final Vector3 a, final Vector3 b) {
				z += b.z;
				s += (a.y + b.y) * (b.x - b.x);
				n += 1;
			}

			@SuppressWarnings("boxing")
			@Override
			public Float result() {
				return volume / 2;
			}
		};
	}

	public static Visitor<Vector3, Vector3> centerOfMass() {
		return new Visitor<Vector3, Vector3>() {
			private float x, y, z;
			private float volume = 0;
			private Vector3 a, b;

			@Override
			public void begin() {
				a = b = null;
			}

			@Override
			public void visit(final Vector3 c) {
				if (a == null) {
					a = c;
					return;
				}
				if (b != null) {
					final float v = a.mixed(b, c);
					x += v * (a.x + b.x + c.x);
					y += v * (a.y + b.y + c.y);
					z += v * (a.z + b.z + c.z);
					volume += v;
				}
				b = c;
			}

			@Override
			public Vector3 result() {
				return Vector3.div(x, y, z, volume * 4);
			}
		};
	}

	/** Origin is assumed to be in the center of mass of Polyhedron. Density is 1. */
	public static Visitor<Vector3, Matrix3> inertiaTensor() {
		return new Visitor<Vector3, Matrix3>() {
			private static final float p = 1 / 60.f, q = 1 / 120.f;
			final Matrix3 canonical = Matrix3.row(p, q, q, q, p, q, q, q, p);

			private Matrix3 covariance = Matrix3.Zero;
			private Vector3 a, b;

			@Override
			public void begin() {
				a = b = null;
			}

			@Override
			public void visit(final Vector3 c) {
				if (a == null) {
					a = c;
					return;
				}
				if (b != null) {
					final Matrix3 m = Matrix3.row(a, b, c);
					covariance = covariance.add(m.transposedMul(canonical).mul(m), m.det());
				}
				b = c;
			}

			@Override
			public Matrix3 result() {
				return Matrix3.diagonal(covariance.trace()).sub(covariance);
			}
		};
	}
}