package tintor.geometry;

import java.util.List;

/** Immutable convex 3d polygon. */
public class Polygon3 {
	private final Vector3[] vertices;
	public final Plane3 plane;

	public Polygon3(final List<Vector3> vertices) {
		if (vertices == null || vertices.size() < 3)
			throw new IllegalArgumentException("Less than 3 vertices. " + vertices);
		// TODO check if planar
		// TODO check if polygon
		// TODO check if convex polygon
		this.vertices = vertices.toArray(new Vector3[vertices.size()]);
		plane = Plane3.create(this.vertices);
	}

	private Polygon3(final Vector3[] vertices, final Plane3 plane) {
		this.vertices = vertices;
		this.plane = plane;
	}

	public int size() {
		return vertices.length;
	}

	public Vector3 get(final int index) {
		return vertices[index];
	}

	public Polygon3 translate(final Vector3 a) {
		final Vector3[] w = new Vector3[vertices.length];
		for (int i = 0; i < w.length; i++)
			w[i] = vertices[i].add(a);
		return new Polygon3(w, plane.move(plane.unitNormal.dot(a)));
	}

	public double surface() {
		double s = 0;
		for (int i = 2; i < vertices.length; i++)
			s += Geometry3.parallelogramSurface(vertices[i].sub(vertices[0]), vertices[i - 1].sub(vertices[0]));
		return s / 2;
	}
}
