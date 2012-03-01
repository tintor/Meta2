package tintor.apps.rigidbody.tools;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import tintor.geometry.Geometry3;
import tintor.geometry.Line3;
import tintor.geometry.Polygon3;
import tintor.geometry.Vector3;
import tintor.util.Visitor;

public class Polyhedrons {
	public static Visitor<Vector3, Polygon3[]> createBuilder() {
		final List<Polygon3> faces = new ArrayList<Polygon3>();
		final List<Vector3> face = new ArrayList<Vector3>();

		return new Visitor<Vector3, Polygon3[]>() {
			@Override
			public void begin() {
				face.clear();
			}

			@Override
			public void visit(final Vector3 vertex) {
				face.add(vertex);
			}

			@Override
			public void end() {
				faces.add(new Polygon3(face));
			}

			@Override
			public Polygon3[] result() {
				return faces.toArray(new Polygon3[faces.size()]);
			}
		};
	}

	/** outside - Positive
	 *  border - Zero
	 *  inside - Negative */
	public static Side side(final Polygon3[] poly, final Vector3 a, final float eps) {
		boolean border = false;
		for (final Polygon3 p : poly) {
			final float d = p.plane.distance(a);
			if (d > eps) return Side.Positive;
			if (d >= -eps) border = true;
		}
		return border ? Side.Zero : Side.Negative;
	}

	/** ASSUMES that polyhedron has no holes in surface! */
	public static Line3[] edges(final Polygon3[] poly) {
		int e = 0;
		for (final Polygon3 f : poly)
			e += f.size();
		final Line3[] edges = new Line3[e / 2];
		e = 0;
		for (final Polygon3 f : poly)
			for (int j = f.size() - 1, i = 0; i < f.size(); j = i++) {
				final Vector3 a = f.get(j), b = f.get(i);
				if (a.compareTo(b) < 0) edges[e++] = Line3.create(a, b);
			}
		assert e == edges.length;
		return edges;
	}

	//	public static Vector3[] vertices(final Polygon3[] poly) {
	//		final Set<Vector3> set = new IdentityHashSet<Vector3>();
	//		for (final Polygon3 f : poly)
	//			for (final Vector3 v : f.vertices)
	//				set.add(v);
	//		return set.toArray(new Vector3[set.size()]);
	//	}
	//	public static Polygon3[] transform(final Polygon3[] poly, final Transform3 transform) {
	//		for (final Polygon3 p : poly)
	//			p.transform(transform);
	//		return poly;
	//	}
	//	public static boolean convex(final Polygon3[] poly) {
	//		for (final Polygon3 p : poly) {
	//			final Vector3 a = Vector3.average(p.vertices);
	//			for (final Polygon3 q : poly)
	//				if (Side.classify(q.plane.distance(a)) == Side.Positive) return false;
	//		}
	//		return true;
	//	}

	public static <Result> Result populate(final Polygon3[] faces, final Visitor<Vector3, Result> visitor) {
		for (final Polygon3 face : faces) {
			visitor.begin();
			for (int i = 0; i < face.size(); i++)
				visitor.visit(face.get(i));
			visitor.end();
		}
		return visitor.result();
	}

	public static void moveToCOM(final Polygon3[] faces, final Vector3[] vertices) {
		final Vector3 centerOfMass = populate(faces, Geometry3.centerOfMass());

		final Map<Vector3, Vector3> map = new IdentityHashMap<Vector3, Vector3>();
		for (int i = 0; i < vertices.length; i++) {
			final Vector3 p = vertices[i].sub(centerOfMass);
			map.put(vertices[i], p);
			vertices[i] = p;
		}

		// translate faces
		for (int i = 0; i < faces.length; i++) {
			final List<Vector3> list = new ArrayList<Vector3>();
			for (int j = 0; j < faces[i].size(); j++)
				list.add(map.get(faces[i].get(j)));
			faces[i] = new Polygon3(list);
		}
	}

	/** Can modify argument. */
	public static Polygon3[] mergeFaces(final Polygon3[] poly) {
		// TODO polyhedrons merge faces
		return poly;
	}

	/** Can modify argument. */
	public static Polygon3[] removeDumbVertices(final Polygon3[] poly) {
		// TODO polyhedrons removeDumbVertices
		// pronadji dve povrsine sa istim ravnima, temenima ABD / DBC i B = lin(A,C)
		// pomeri B prema brizem od A/C ako povrsine ostaju 
		return poly;
	}

	/** Can modify argument. */
	//	public static Polygon3[] mergeCloseVertices(final Polygon3[] poly, float eps) {
	//		final Set<Polygon3> updated = new IdentityHashSet<Polygon3>();
	//		final Map<Vector3, Integer> weight = new IdentityHashMap<Vector3, Integer>();
	//
	//		eps *= eps;
	//		//		boolean change = true;
	//		//		while (change) {
	//		//			change = false;
	//		//			int c = 0;
	//
	//		for (int pi = 0; pi < poly.length; pi++)
	//			for (int pj = pi; pj < poly.length; pj++) {
	//				final Vector3[] a = poly[pi].vertices, b = poly[pj].vertices;
	//
	//				for (int i = 0; i < a.length; i++)
	//					for (int j = 0; j < b.length; j++)
	//						if (a[i] != b[j] && a[i].distanceSquared(b[j]) <= eps) {
	//							final int wa = weight.containsKey(a[i]) ? weight.get(a[i]) : 1;
	//							final int wb = weight.containsKey(b[j]) ? weight.get(b[j]) : 1;
	//
	//							final Vector3 v = a[i] = b[j] = Vector3.linear(a[i], b[j], wa / (wa + wb));
	//							weight.put(v, wa + wb);
	//
	//							updated.add(poly[pi]);
	//							updated.add(poly[pj]);
	//							//								change = true;
	//							//								c++;
	//						}
	//			}
	//
	//		// TODO remove empty polygons
	//		for (final Polygon3 c : updated) {
	//			c.removeDuplicates();
	//			c.plane = new Plane3(c.vertices);
	//		}
	//
	//		return poly;
	//	}
	//	public static Polygon3[] difference(final Polygon3[] a, final Polygon3[] b) {
	//		return operation(a, b, Outside, InsideFlip);
	//	}
	//
	//	public static Polygon3[] intersection(final Polygon3[] a, final Polygon3[] b) {
	//		return operation(a, b, Inside, Inside);
	//	}
	//
	//	public static Polygon3[] union(final Polygon3[] a, final Polygon3[] b) {
	//		return operation(a, b, Outside, Outside);
	//	}
	enum Policy {
		Accept, Flip, Reject;
	}

	// N O T E depends on order of Side.* constants
	//private static final Policy[] Outside = { Policy.Accept, Policy.Accept, Policy.Reject };
	//private static final Policy[] Inside = { Policy.Reject, Policy.Accept, Policy.Accept };
	//private static final Policy[] OutsideFlip = { Policy.Flip, Policy.Flip, Policy.Reject };
	//private static final Policy[] InsideFlip = { Policy.Reject, Policy.Flip, Policy.Flip };

	//private static final Policy[] AcceptAll = { Policy.Accept, Policy.Accept, Policy.Accept };

	//	public static Polygon3[] operation(final Polygon3[] a, final Polygon3[] b, final Policy[] policyA,
	//			final Policy[] policyB) {
	//		final List<Polygon3> q = new ArrayList<Polygon3>();
	//		if (policyA != null) half(a, b, policyA, q);
	//		if (policyB != null) half(b, a, policyB, q);
	//		return q.toArray(new Polygon3[q.size()]);
	//	}
	//
	//	static void half(final Polygon3[] a, final Polygon3[] b, final Policy[] policy, final List<Polygon3> q) {
	//		List<Polygon3> r = new ArrayList<Polygon3>(), w = new ArrayList<Polygon3>();
	//		final float eps = 1e-8f;
	//
	//		for (final Polygon3 pa : a)
	//			r.add(pa);
	//
	//		// cut every polygon from A with every polygon from B
	//		for (final Polygon3 pb : b) {
	//			w.clear();
	//			for (final Polygon3 pa : r)
	//				if (pa.penetrating(pb, eps)) {
	//					final Polygon3[] v = pa.split(pb.plane, eps);
	//					assert v[0].vertices.length >= 3 && v[1].vertices.length >= 3;
	//					w.add(v[0]);
	//					w.add(v[1]);
	//				} else
	//					w.add(pa);
	//
	//			final List<Polygon3> t = r;
	//			r = w;
	//			w = t;
	//		}
	//
	//		// select pieces of A based on policy
	//		for (Polygon3 face : r) {
	//			final Side s = Side.classify(signedDistanceSquared(b, Vector3.average(face.vertices)), eps);
	//			final Policy p = policy[s.ordinal()];
	//			if (p != Policy.Reject) {
	//				face = face.clone();
	//				if (p == Policy.Flip) face = face.flip();
	//				q.add(face);
	//			}
	//		}
	//	}

	//	public static Interval interval(final Polygon3[] poly, final Vector3 normal) {
	//		final Interval i = new Interval();
	//		for (final Polygon3 p : poly)
	//			i.union(p.interval(normal));
	//		return i;
	//	}
	//	public static Polygon3 planePolygon(final Polygon3[] poly, final Plane3 plane) {
	//		final Vector3 ex = plane.normal.normal().unit(), ey = plane.normal.cross(ex);
	//		assert ex.isFinite() && ey.isFinite();
	//		final Interval ix = interval(poly, ex), iy = interval(poly, ey);
	//		final Vector3 center = ex.mul(ix.center()).add(iy.center(), ey).sub(plane.offset, plane.normal);
	//
	//		final Vector3 a = center.add(ix.width(), ex).add(iy.width(), ey);
	//		final Vector3 b = center.sub(ix.width(), ex).add(iy.width(), ey);
	//		final Vector3 c = center.sub(ix.width(), ex).sub(iy.width(), ey);
	//		final Vector3 d = center.add(ix.width(), ex).sub(iy.width(), ey);
	//		return new Polygon3(plane, a, b, c, d);
	//	}
	//	static Polygon3[] intersect(final Polygon3[] poly, final Plane3 plane, final boolean halfspace) {
	//		final Polygon3[] p = { planePolygon(poly, plane) };
	//		return operation(poly, p, halfspace ? Inside : null, Inside);
	//	}
	//	public static Polygon3[] optimize(Polygon3[] poly, final float eps) {
	//		poly = mergeCloseVertices(poly, eps);
	//		poly = removeDumbVertices(poly);
	//		return mergeFaces(poly);
	//	}
	//	public static void splitIntoConvex(Polygon3[] poly, final List<Polygon3[]> list) {
	//		poly = optimize(poly, 1e-8f);
	//		if (convex(poly)) {
	//			list.add(poly);
	//			return;
	//		}
	//
	//		final float v = signedVolume(poly);
	//		float dmin = Float.POSITIVE_INFINITY;
	//		Polygon3[] amin = null, bmin = null;
	//
	//		for (final Polygon3 p : poly) {
	//			final Polygon3[] c = { planePolygon(poly, p.plane) };
	//
	//			final Polygon3[] a = intersection(poly, c);
	//			final float va = signedVolume(a);
	//
	//			c[0].flip();
	//
	//			final Polygon3[] b = intersection(poly, c);
	//			final float vb = signedVolume(b);
	//
	//			// System.out.println("log: va:" + va + " vb:" + vb + " va+vb-v:" + (va + vb - v));
	//			assert Math.abs(va + vb - v) < 1e-5 : va + " " + vb + " " + v;
	//
	//			final float d = Math.abs(va - vb);
	//			if (d < dmin) {
	//				amin = a;
	//				bmin = b;
	//				dmin = d;
	//			}
	//		}
	//
	//		splitIntoConvex(amin, list);
	//		splitIntoConvex(bmin, list);
	//	}
	//	public static void render(final Polygon3[] poly) {
	//		for (final Polygon3 p : poly)
	//			p.render();
	//	}

	//	@SuppressWarnings("null")
	//	public static float signedDistanceSquared(final Polygon3[] poly, final Vector3 a) {
	//		float dmin = Float.POSITIVE_INFINITY;
	//		Polygon3 pmin = null;
	//
	//		for (final Polygon3 p : poly) {
	//			final float d = p.distanceSquared(a);
	//			assert !Float.isNaN(d);
	//			if (d < dmin) {
	//				pmin = p;
	//				dmin = d;
	//			}
	//		}
	//
	//		return pmin.plane.distance(a) >= 0 ? dmin : -dmin;
	//	}
}