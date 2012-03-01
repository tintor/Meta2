package tintor.apps.rigidbody.model.solid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import tintor.apps.rigidbody.tools.GLA;
import tintor.apps.rigidbody.tools.Polyhedrons;
import tintor.geometry.ConvexHull3;
import tintor.geometry.Geometry3;
import tintor.geometry.Interval;
import tintor.geometry.Line3;
import tintor.geometry.Matrix3;
import tintor.geometry.Polygon3;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.util.Visitor;

/** Describes structure of convex polyhedron */
final class Polyhedron extends Convex {
	final Polygon3[] faces;
	final Line3[] edges;
	final Vector3[] vertices;
	final Interval[] intervals;

	// Constructors
	Polyhedron(final List<Vector3> w) {
		final ConvexHull3 hull = new ConvexHull3(w, true);

		vertices = hull.visitVertices(new Visitor<Vector3, Vector3[]>() {
			List<Vector3> list = new ArrayList<Vector3>();

			@Override
			public void visit(final Vector3 v) {
				list.add(v);
			}

			@Override
			public Vector3[] result() {
				return list.toArray(new Vector3[list.size()]);
			}
		});

		faces = hull.visit(Polyhedrons.createBuilder());

		final Map<Polygon3, Double> surface = new IdentityHashMap<Polygon3, Double>();
		for (final Polygon3 face : faces)
			surface.put(face, face.surface());

		Arrays.sort(faces, new Comparator<Polygon3>() {
			@Override
			public int compare(final Polygon3 a, final Polygon3 b) {
				return Double.compare(surface.get(b), surface.get(b));
			}
		});

		intervals = new Interval[faces.length];
		for (int i = 0; i < faces.length; i++)
			intervals[i] = interval(faces[i].plane.unitNormal);

		edges = Polyhedrons.edges(faces);

		final Vector3 sum = Vector3.Zero;
		for (final Vector3 v : vertices)
			sum.add(v);
		sphereCenter = sum.div(vertices.length);
		sphereRadius = maximal(sphereCenter);
	}

	/** Polyhedron must not be scaled! */
	private Polyhedron(final Polyhedron poly, final Transform3 transform) {
		edges = new Line3[poly.edges.length];
		faces = new Polygon3[poly.faces.length];
		vertices = new Vector3[poly.vertices.length];

		// translate vertices
		final Map<Vector3, Vector3> map = new IdentityHashMap<Vector3, Vector3>();
		for (int i = 0; i < poly.vertices.length; i++) {
			final Vector3 p = transform.applyVector(poly.vertices[i]);
			map.put(poly.vertices[i], p);
			vertices[i] = p;
		}

		// translate edges
		for (int i = 0; i < poly.edges.length; i++)
			edges[i] = Line3.create(map.get(poly.edges[i].a), map.get(poly.edges[i].b));

		// translate faces
		final List<Vector3> list = new ArrayList<Vector3>();
		for (int i = 0; i < poly.faces.length; i++) {
			list.clear();
			for (int j = 0; j < faces[i].size(); j++)
				list.add(map.get(poly.faces[i].get(j)));
			faces[i] = new Polygon3(list);
		}

		// copy intervals
		intervals = poly.intervals;

		sphereCenter = transform.applyPoint(poly.sphereCenter);
		sphereRadius = poly.sphereRadius;
	}

	public <Result> Result visit(final Visitor<Vector3, Result> visitor) {
		for (final Polygon3 face : faces) {
			visitor.begin();
			for (int i = 0; i < face.size(); i++)
				visitor.visit(face.get(i));
			visitor.end();
		}
		return visitor.result();
	}

	@Override
	public final Vector3 centerOfMass() {
		return visit(Geometry3.centerOfMass());
	}

	@Override
	public float mass() {
		return Math.abs(visit(Geometry3.signedVolume()));
	}

	@Override
	public Matrix3 inertiaTensor() {
		// FIXME when not in center of mass
		return visit(Geometry3.inertiaTensor());
	}

	@Override
	public float maximal(final Vector3 center) {
		double r = 0;
		for (final Vector3 v : vertices)
			r = Math.max(r, v.distanceSquared(center));
		return (float) Math.sqrt(r);
	}

	@Override
	public float distance(final Vector3 point) {
		// FIXME doesn't work for positive

		float distance = Float.NEGATIVE_INFINITY;
		for (final Polygon3 f : faces) {
			final float d = f.plane.distance(point);
			if (d > distance) distance = d;
		}
		//		if (distance > 0) for (final Line3 e : edges) {
		//			
		//			e.distanceSquared(point);
		//
		//		}
		//return distance;
		throw new UnsupportedOperationException();
	}

	@Override
	public void render() {
		for (final Polygon3 face : faces) {
			GLA.normal(face.plane.unitNormal);
			GLA.beginPolygon();
			for (int i = 0; i < face.size(); i++)
				GLA.vertex(face.get(i));
			GLA.end();
		}
	}

	@Override
	public Interval interval(final Vector3 normal) {
		float min = normal.dot(vertices[0]), max = min;
		for (int i = 1; i < vertices.length; i++) {
			final float d = normal.dot(vertices[i]);
			if (d > max)
				max = d;
			else if (d < min) min = d;
		}
		return Interval.create(min, max);
	}

	/** returns part of line that is inside poly, or null if completely outside */
	Line3 clip(final Line3 line) {
		return Geometry3.convexClip(faces, line.a, line.b, 0);
	}

	@Override
	public Polyhedron transform(final Transform3 transform) {
		if (transform == Transform3.Identity) return this;
		return new Polyhedron(this, transform);
	}
}