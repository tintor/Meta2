package tintor.geometry;

/** @see "vcg.isti.cnr.it/~ponchio/computergraphics/exercises/plucker.pdf" */
public class Plucker {
	public static Plucker create(Line3 line) {
		return line(line.a, line.b);
	}

	public static Plucker create(Ray3 ray) {
		return ray(ray.origin, ray.unitDir);
	}

	public static Plucker line(final Vector3 a, final Vector3 b) {
		return new Plucker(b.sub(a), b.cross(a));
	}

	public static Plucker ray(final Vector3 origin, final Vector3 dir) {
		return new Plucker(dir, dir.cross(origin));
	}

	private final Vector3 u, v;

	private Plucker(final Vector3 u, final Vector3 v) {
		this.u = u;
		this.v = v;
	}

	/**
	 * <0 Clockwise (if you look in direction of one line, other will go CW around it) =0 Intersect or Parallel >0
	 * Counterclockwise
	 */
	public float side(final Plucker p) {
		return u.dot(p.v) + v.dot(p.u);
	}

	public float side(final Vector3 a, final Vector3 b) {
		return u.mixed(a, b) + v.dot(a, b);
	}

	public float side(final Line3 p) {
		return u.mixed(p.a, p.b) + v.dot(p.a, p.b);
	}

	public float side(final Ray3 p) {
		return u.mixed(p.unitDir, p.origin) + v.dot(p.unitDir);
	}
}