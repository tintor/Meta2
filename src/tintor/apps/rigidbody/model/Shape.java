package tintor.apps.rigidbody.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL;

import tintor.apps.rigidbody.tools.ConvexPolyhedrons;
import tintor.apps.rigidbody.tools.GLA;
import tintor.apps.rigidbody.tools.Polyhedrons;
import tintor.apps.rigidbody.tools.Side;
import tintor.geometry.ConvexHull3;
import tintor.geometry.Geometry3;
import tintor.geometry.Interval;
import tintor.geometry.Line3;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Polygon3;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;
import tintor.util.Visitor;

enum Hint {
	Sphere, Box, Convex
}

public final class Shape {
	public static boolean useHints = true;

	public static Shape box(final float sizeX, final float sizeY, final float sizeZ) {
		final Shape b = new Shape(Hint.Box, ConvexPolyhedrons.cube(sizeX / 2, sizeY / 2, sizeZ / 2));
		b.boxX = Interval.create(-sizeX / 2, sizeX / 2);
		b.boxY = Interval.create(-sizeY / 2, sizeY / 2);
		b.boxZ = Interval.create(-sizeZ / 2, sizeZ / 2);
		return b;
	}

	public static Shape sphere(final float size, final int seg) {
		return new Shape(Hint.Sphere, ConvexPolyhedrons.sphere(size / 2, seg));
	}

	public static Shape sphere(final float size, final int seg, final Vector3 color1, final Vector3 color2) {
		final Shape s = sphere(size, seg);
		s.bicolor(Vector3.X, color1, color2);
		return s;
	}

	// Fields
	public final Polygon3[] faces; // sorted by size
	final Line3[] edges;
	final Vector3[] vertices;
	final Interval[] intervals;
	public final float radius;
	private final Hint hint;

	private Interval boxX, boxY, boxZ;

	// Constructors
	public Shape(final Vector3... w) {
		this(Hint.Convex, w);
	}

	private static Visitor<Vector3, Vector3[]> arrayBuilder() {
		final List<Vector3> list = new ArrayList<Vector3>();

		return new Visitor<Vector3, Vector3[]>() {
			@Override
			public void visit(final Vector3 element) {
				list.add(element);
			}

			@Override
			public Vector3[] result() {
				return list.toArray(new Vector3[list.size()]);
			}
		};
	}

	private Shape(final Hint hint, final Vector3... w) {
		this.hint = hint;

		final ConvexHull3 hull = new ConvexHull3(Arrays.asList(w), true);
		faces = hull.visit(Polyhedrons.createBuilder());
		vertices = hull.visitVertices(arrayBuilder());
		Polyhedrons.moveToCOM(faces, vertices);
		edges = Polyhedrons.edges(faces);
		radius = radius();

		// intervals
		intervals = new Interval[faces.length];
		for (int i = 0; i < faces.length; i++)
			intervals[i] = interval(faces[i].plane.unitNormal, vertices);
	}

	private float radius() {
		float r = 0;
		for (final Vector3 v : vertices)
			r = Math.max(r, v.square());
		return (float) Math.sqrt(r);
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

	public float volume() {
		switch (hint) {
		case Sphere:
			return radius * radius * radius * (float) (Math.PI * 4 / 3);
		case Box:
			return boxX.max * boxY.max * boxZ.max * 8;
		default:
			return Math.abs(visit(Geometry3.signedVolume()));
		}
	}

	public Matrix3 inertiaTensor() {
		return visit(Geometry3.inertiaTensor());
	}

	private int glList = Integer.MIN_VALUE;

	public void render() {
		if (glList == Integer.MIN_VALUE) {
			glList = GLA.gl.glGenLists(1);
			GLA.gl.glNewList(glList, GL.GL_COMPILE);
			for (final Polygon3 face : faces) {
				// if (face.color != null) GLA.color(face.color);
				GLA.beginPolygon();
				GLA.normal(face.plane.unitNormal);
				for (int i = 0; i < face.size(); i++)
					GLA.vertex(face.get(i));
				GLA.gl.glEnd();
			}
			GLA.gl.glEndList();
		}
		GLA.gl.glCallList(glList);
	}

	public void bicolor(final Vector3 axis, final Vector3 color1, final Vector3 color2) {
		//for (final Polygon3 p : faces)
		//	p.color = axis.dot(p.plane.unitNormal) > 0 ? color1 : color2;
	}

	/** Axis is in localspace */
	public Interval interval(final Vector3 axis) {
		switch (hint) {
		case Sphere:
			return Interval.create(-radius, radius);
		case Box:
			final float m = Math.abs(axis.x) * boxX.max + Math.abs(axis.y) * boxY.max + Math.abs(axis.z)
					* boxZ.max;
			return Interval.create(-m, m);
		default:
			return interval(axis, vertices);
		}
	}

	private static Interval interval(final Vector3 dir, final Vector3... array) {
		float min = dir.dot(array[0]), max = min;
		for (int i = 1; i < array.length; i++) {
			final float d = dir.dot(array[i]);
			if (d > max)
				max = d;
			else if (d < min) min = d;
		}
		return Interval.create(min, max);
	}

	/** Project body onto axis WITHOUT offset! */
	private static Interval interval(final Body body, final Vector3 axis) {
		return body.solid.interval(body.transform().iapplyVector(axis));
	}

	private static float maxDist;
	private static Vector3 maxAxis;
	private static Vector3 offset;

	private static boolean separating(Vector3 axis, final Interval a, final Interval b) {
		float z = a.min - b.min + a.max - b.max - offset.dot(axis);
		if (z < 0) {
			z = -z;
			axis = axis.neg();
		}
		final float dist = (z + a.min - a.max + b.min - b.max) / 2;
		if (dist > 0) return true;
		if (dist > maxDist) {
			maxDist = dist;
			maxAxis = axis;
		}
		return false;
	}

	private static float square(final float a) {
		return a * a;
	}

	/** Algoritm tests candidates (from faces and edges) for separation axis.
	 *  If no separation axis is found, contact is assumed. */
	public static Contact findContact(Body a, Body b) {
		// cheap bounding spheres test
		if (a.position().distanceSquared(b.position()) > square(a.solid.radius + b.solid.radius)) return null;

		// sortiraj to broju ivica prvo / malo ubrzava SAT
		if (a.id > b.id) {
			final Body t = a;
			a = b;
			b = t;
		}
		maxDist = Float.NEGATIVE_INFINITY;
		maxAxis = null;
		offset = b.position().sub(a.position()).mul(2); // = (b.pos-a.pos)*2

		// Sphere / Sphere
		if (a.solid.hint == Hint.Sphere && b.solid.hint == Hint.Sphere) return findContactSphereSphere(a, b);

		// Sphere / Box
		if (false && a.solid.hint == Hint.Sphere && b.solid.hint == Hint.Box) return findContactSphereBox(a, b);
		if (false && a.solid.hint == Hint.Box && b.solid.hint == Hint.Sphere) {
			offset = offset.neg();
			return findContactSphereBox(b, a);
		}

		// Box / Box
		if (a.solid.hint == Hint.Box && b.solid.hint == Hint.Box) return findContactBoxBox(a, b);

		// Sphere / Poly 
		if (false && a.solid.hint == Hint.Sphere && b.solid.hint == Hint.Convex)
			return findContactSpherePoly(a, b);
		if (false && a.solid.hint == Hint.Convex && b.solid.hint == Hint.Sphere) {
			offset = offset.neg();
			return findContactSpherePoly(b, a);
		}

		return findContactPolyPoly(a, b);
	}

	private static Contact findContactPolyPoly(final Body a, final Body b) {
		// Use axis from Arbiter
		final Arbiter arbiter = Arbiter.get(a, b);
		if (arbiter.axis != null && arbiter.impulse == Vector3.Zero)
			if (separating(arbiter.axis, interval(a, arbiter.axis), interval(b, arbiter.axis))) return null;

		// O(a.faces * b.vertices)
		for (int i = 0; i < a.solid.faces.length; i++) {
			final Vector3 axis = a.transform().applyVector(a.solid.faces[i].plane.unitNormal);
			if (separating(axis, a.solid.intervals[i], interval(b, axis))) {
				arbiter.impulse = Vector3.Zero;
				arbiter.axis = axis;
				return null;
			}
		}

		// O(a.faces * b.vertices)
		for (int i = 0; i < b.solid.faces.length; i++) {
			final Vector3 axis = b.transform().applyVector(b.solid.faces[i].plane.unitNormal);
			if (separating(axis, interval(a, axis), b.solid.intervals[i])) {
				arbiter.impulse = Vector3.Zero;
				arbiter.axis = axis;
				return null;
			}
		}

		// O(a.edges * b.edges * (a.vertices + b.vertices))
		for (final Line3 ea : a.solid.edges) {
			final Vector3 da = a.transform().applyVector(ea.direction());
			for (final Line3 eb : b.solid.edges) {
				final Vector3 db = b.transform().applyVector(eb.direction());
				final Vector3 axis = da.cross(db).unit();
				if (!axis.isFinite()) continue;
				if (separating(axis, interval(a, axis), interval(b, axis))) {
					arbiter.impulse = Vector3.Zero;
					arbiter.axis = axis;
					return null;
				}
			}
		}

		final Vector3 z = intersectBodyBody(a, b);
		if (z == null) return null;
		return new Contact(a, b, maxAxis, z, -maxDist, Arbiter.get(a, b));
	}

	private static Contact findContactSphereSphere(final Body a, final Body b) {
		final Vector3 d = a.position().sub(b.position());
		final float dist = (float) d.length();

		Vector3 axis = d.div(dist);
		if (!axis.isFinite()) axis = Vector3.X;
		final Vector3 point = Vector3.average(a.position().add(b.solid.radius, axis), b.position().sub(
				a.solid.radius, axis)); // TODO fix this!

		return new Contact(a, b, axis, point, a.solid.radius + b.solid.radius - dist, Arbiter.get(a, b));
	}

	private static Contact findContactSphereBox(final Body a, final Body b) {
		if (true) throw new RuntimeException();

		final Vector3[] bn = new Vector3[3];
		if (separating(bn[0] = b.transform().rotation.colX(), interval(a, bn[0]), b.solid.boxX)) return null;
		if (separating(bn[1] = b.transform().rotation.colY(), interval(a, bn[1]), b.solid.boxY)) return null;
		if (separating(bn[2] = b.transform().rotation.colZ(), interval(a, bn[2]), b.solid.boxZ)) return null;

		final Vector3 axis = null; // TODO from sphere center to nearest feature on Box
		if (separating(axis, interval(a, axis), interval(b, axis))) return null;

		return new Contact(a, b, maxAxis, intersectBodyBody(a, b), -maxDist, Arbiter.get(a, b));
	}

	private static Contact findContactSpherePoly(final Body a, final Body b) {
		if (true) throw new RuntimeException();

		// O(a.faces * b.vertices)
		for (final Polygon3 element : b.solid.faces) {
			final Vector3 axis = b.transform().applyVector(element.plane.unitNormal);
			if (separating(axis, interval(a, axis), interval(b, axis))) return null;
		}

		final Vector3 axisS = null; // TODO from sphere center to nearest feature on Polygon 
		if (separating(axisS, Interval.create(-a.solid.radius, a.solid.radius), interval(b, axisS))) return null;

		return new Contact(a, b, maxAxis, intersectBodyBody(a, b), -maxDist, Arbiter.get(a, b));
	}

	private static Contact findContactBoxBox(final Body a, final Body b) {
		final Vector3[] an = new Vector3[3];
		if (separating(an[0] = a.transform().rotation.colX(), a.solid.boxX, interval(b, an[0]))) return null;
		if (separating(an[1] = a.transform().rotation.colY(), a.solid.boxY, interval(b, an[1]))) return null;
		if (separating(an[2] = a.transform().rotation.colZ(), a.solid.boxZ, interval(b, an[2]))) return null;

		final Vector3[] bn = new Vector3[3];
		if (separating(bn[0] = b.transform().rotation.colX(), interval(a, bn[0]), b.solid.boxX)) return null;
		if (separating(bn[1] = b.transform().rotation.colY(), interval(a, bn[1]), b.solid.boxY)) return null;
		if (separating(bn[2] = b.transform().rotation.colZ(), interval(a, bn[2]), b.solid.boxZ)) return null;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				final Vector3 axis = an[i].cross(bn[j]).unit();
				if (!axis.isFinite()) continue;
				if (separating(axis, interval(a, axis), interval(b, axis))) return null;
			}

		final Vector3 p = intersectBodyBody(a, b);
		if (p == null) return null;
		return new Contact(a, b, maxAxis, p, -maxDist, Arbiter.get(a, b));
	}

	public static Contact findContact(final Body a, final Plane3 p) {
		// cheap bounding sphere test
		if (p.distance(a.position()) > a.solid.radius) return null;

		if (a.solid.hint == Hint.Sphere)
			return new Contact(a, World.Space, p.unitNormal, a.position().sub(a.solid.radius, p.unitNormal),
					a.solid.radius - p.distance(a.position()), Arbiter.get(a, World.Space));

		final float dist = a.interval(p.unitNormal).min + p.offset;
		if (dist > 0) return null;
		final Vector3 z = intersectBodyPlane(a, p);
		if (z == null) return null;
		return new Contact(a, World.Space, p.unitNormal, z, -dist, Arbiter.get(a, World.Space));
	}

	public static int met = 2;

	public static Vector3 intersectBodyBody(final Body a, final Body b) {
		switch (met) {
		//		case 1: {
		//			final List<Vector3> list = new ArrayList<Vector3>();
		//
		//			final Transform3 transform = a.transform().icombine(b.transform());
		//			for (final Line3 e : a.shape.edges) {
		//				final Line3 p = Polyhedrons.convexClip(b.shape.faces, transform.apply(e));
		//				if (p != null) {
		//					list.add(b.transform().applyPoint(p.a));
		//					if (p.a != p.b) list.add(b.transform().applyPoint(p.b));
		//				}
		//			}
		//			for (final Line3 e : b.shape.edges) {
		//				final Line3 p = Polyhedrons.convexClip(a.shape.faces, transform.iapply(e));
		//				if (p != null) {
		//					list.add(a.transform().applyPoint(p.a));
		//					if (p.a != p.b) list.add(a.transform().applyPoint(p.b));
		//				}
		//			}
		//
		//			return Vector3.average(list);
		//		}
		case 2: {
			final List<Vector3> list = new ArrayList<Vector3>();

			final Transform3 transform = a.transform().icombine(b.transform());
			for (final Line3 e : a.solid.edges) {
				final Line3 p = Geometry3.convexClip(b.solid.faces, transform.apply(e), 0);
				if (p != null) {
					if (p.a != e.a) list.add(b.transform().applyPoint(p.a));
					if (p.b != p.a && p.b != e.b) list.add(b.transform().applyPoint(p.b));
				}
			}
			for (final Vector3 v : a.solid.vertices)
				if (Polyhedrons.side(b.solid.faces, transform.applyPoint(v), 0) != Side.Positive)
					list.add(a.transform().applyPoint(v));

			for (final Line3 e : b.solid.edges) {
				final Line3 p = Geometry3.convexClip(a.solid.faces, transform.iapply(e), 0);
				if (p != null) {
					if (p.a != e.a) list.add(a.transform().applyPoint(p.a));
					if (p.b != p.a && p.b != e.b) list.add(a.transform().applyPoint(p.b));
				}
			}
			for (final Vector3 v : b.solid.vertices)
				if (Polyhedrons.side(a.solid.faces, transform.iapplyPoint(v), 0) != Side.Positive)
					list.add(b.transform().applyPoint(v));

			if (list.size() == 0) return null;
			return Vector3.average(list);
		}
			//		case 3: {
			//			final List<Vector3> list = new ArrayList<Vector3>();
			//
			//			final Transform3 transform = a.transform().icombine(b.transform());
			//			for (final Line3 e : a.shape.edges) {
			//				final Line3 p = Polyhedrons.convexClip(b.shape.faces, transform.apply(e));
			//				if (p != null) {
			//					if (p.a != e.a) list.add(b.transform().applyPoint(p.a));
			//					if (p.b != p.a && p.b != e.b) list.add(b.transform().applyPoint(p.b));
			//				}
			//			}
			//			for (final Vector3 v : a.shape.vertices)
			//				if (Polyhedrons.side(b.shape.faces, transform.applyPoint(v)) != Side.Positive)
			//					list.add(a.transform().applyPoint(v));
			//
			//			for (final Line3 e : b.shape.edges) {
			//				final Line3 p = Polyhedrons.convexClip(a.shape.faces, transform.iapply(e));
			//				if (p != null) {
			//					if (p.a != e.a) list.add(a.transform().applyPoint(p.a));
			//					if (p.b != p.a && p.b != e.b) list.add(a.transform().applyPoint(p.b));
			//				}
			//			}
			//			for (final Vector3 v : b.shape.vertices)
			//				if (Polyhedrons.side(a.shape.faces, transform.iapplyPoint(v)) != Side.Positive)
			//					list.add(b.transform().applyPoint(v));
			//
			//			return new ConvexHull3(list, true).visit(Geometry3.centerOfMass());
			//		}
		}
		return null;
	}

	private static float LineClipEPS = 0;

	public static Vector3 intersectBodyPlane(final Body a, final Plane3 plane) {
		switch (met) {
		//		case 1: {
		//			final List<Vector3> list = new ArrayList<Vector3>();
		//
		//			final Plane3 xplane = a.transform().iapply(plane);
		//			for (final Line3 e : a.shape.edges) {
		//				final Line3 p = e.clip(xplane, LineClipEPS);
		//				if (p != null) {
		//					list.add(a.transform().applyPoint(p.a));
		//					if (p.b != p.a) list.add(a.transform().applyPoint(p.b));
		//				}
		//			}
		//			return Vector3.average(list);
		//		}
		case 2: {
			final List<Vector3> list = new ArrayList<Vector3>();

			final Plane3 xplane = a.transform().iapply(plane);
			for (final Line3 e : a.solid.edges) {
				final Line3 p = e.clip(xplane, LineClipEPS);
				if (p != null) {
					if (p.a != e.a) list.add(a.transform().applyPoint(p.a));
					if (p.b != p.a) if (p.b != e.b) list.add(a.transform().applyPoint(p.b));
				}
			}
			for (final Vector3 v : a.solid.vertices)
				if (xplane.distance(v) <= 0) list.add(a.transform().applyPoint(v));

			if (list.size() == 0) return null;
			return Vector3.average(list);
		}
			//		case 3: {
			//			final List<Vector3> list = new ArrayList<Vector3>();
			//
			//			final Plane3 xplane = a.transform().iapply(plane);
			//			for (final Line3 e : a.shape.edges) {
			//				final Line3 p = e.clip(xplane, LineClipEPS);
			//				if (p != null) {
			//					if (p.a != e.a) list.add(a.transform().applyPoint(p.a));
			//					if (p.b != p.a) if (p.b != e.b) list.add(a.transform().applyPoint(p.b));
			//				}
			//			}
			//			for (final Vector3 v : a.shape.vertices)
			//				if (xplane.distance(v) <= 0) list.add(a.transform().applyPoint(v));
			//
			//			return new ConvexHull3(list, true).visit(Geometry3.centerOfMass());
			//		}
			//		case 4: {
			//			final Center c = new Center();
			//			final Plane3 xplane = a.transform().iapply(plane).flip();
			//			for (final Polygon3 face : a.shape.faces) {
			//				final Polygon3 f2 = face.clip(xplane);
			//				if (f2.size() >= 3) c.add(f2.vertices);
			//			}
			//			return c.center();
			//		}
		}
		return null;
	}

	// Box/Box intersection
	private static void clip(final Body s, Vector3 a, Vector3 b) {
		// clipX
		if (a.x > b.x) {
			final Vector3 t = a;
			a = b;
			b = t;
		}

		final Interval x = s.solid.boxX;
		if (b.x < x.min || a.x > x.max) return;
		if (a.x < x.min) a = Vector3.linear(a, b, (x.min - a.x) / (b.x - a.x));
		if (b.x > x.max) b = Vector3.linear(a, b, (x.max - a.x) / (b.x - a.x));

		// clipY
		if (a.y > b.y) {
			final Vector3 t = a;
			a = b;
			b = t;
		}

		final Interval y = s.solid.boxY;
		if (b.y < y.min || a.y > y.max) return;
		if (a.y < y.min) a = Vector3.linear(a, b, (y.min - a.y) / (b.y - a.y));
		if (b.y > y.max) b = Vector3.linear(a, b, (y.max - a.y) / (b.y - a.y));

		// clipX
		if (a.z > b.z) {
			final Vector3 t = a;
			a = b;
			b = t;
		}

		final Interval z = s.solid.boxZ;
		if (b.z < z.min || a.z > z.max) return;
		if (a.z < z.min) a = Vector3.linear(a, b, (z.min - a.z) / (b.z - a.z));
		if (b.z > z.max) b = Vector3.linear(a, b, (z.max - a.z) / (b.z - a.z));

		// finish
		if (!a.equals(b)) setAdd(s.transform().applyPoint(b));
		setAdd(s.transform().applyPoint(a));
	}

	private final static int Vertices = 8, Edges = 12;
	private final static int[] Ea = { 0, 2, 0, 1, 4, 6, 4, 5, 0, 2, 1, 3 };
	private final static int[] Eb = { 1, 3, 2, 3, 5, 7, 6, 7, 4, 6, 5, 7 };

	private static Vector3[] temp;
	private static int tempSize;

	private static void setAdd(final Vector3 a) {
		for (int i = tempSize - 1; i >= Vertices; i--)
			if (temp[i].equals(a)) return;
		temp[tempSize++] = a;
	}

	// optimizovano do bola!
	public static Vector3[] intersectBoxBox(final Body a, final Body b) {
		final Transform3 transform = a.transform().icombine(b.transform());

		tempSize = Vertices;
		temp = new Vector3[Vertices + Edges * 4]; // NOTE can this be lowered?

		for (int i = 0; i < Vertices; i++)
			temp[i] = transform.applyPoint(a.solid.vertices[i]);
		for (int i = 0; i < Edges; i++)
			clip(b, temp[Ea[i]], temp[Eb[i]]);

		for (int i = 0; i < Vertices; i++)
			temp[i] = transform.iapplyPoint(b.solid.vertices[i]);
		for (int i = 0; i < Edges; i++)
			clip(a, temp[Ea[i]], temp[Eb[i]]);

		final Vector3[] q = new Vector3[tempSize - Vertices];
		assert q.length > 0;
		System.arraycopy(temp, Vertices, q, 0, tempSize - Vertices);
		return q;
	}
}