package tintor.apps.rigidbody.model.solid;

import java.util.ArrayList;
import java.util.List;

import tintor.apps.rigidbody.model.Arbiter;
import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Contact;
import tintor.apps.rigidbody.tools.Polyhedrons;
import tintor.apps.rigidbody.tools.Side;
import tintor.geometry.Geometry3;
import tintor.geometry.Interval;
import tintor.geometry.Line3;
import tintor.geometry.Plane3;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;

public final class Collision {
	final Collision other;

	Body body;
	Solid solid;
	Transform3 transform;
	Material material;

	List<Contact> contacts;

	float contactDist;
	Vector3 contactNormal;
	Vector3 contactPoint;
	Vector3 delta;

	public static Collision create(final List<Contact> contacts) {
		final Collision c = new Collision(null);
		c.contacts = c.other.contacts = contacts;
		return c;
	}

	Collision(final Collision a) {
		other = a != null ? a : new Collision(this);
	}

	private void init(final Body b) {
		body = b;
		solid = b.solid;
		transform = b.transform();

		material = new Material(Solid.Point);
	}

	public void findContacts(final Body a, final Body b) {
		final Vector3 ac = transformPoint(a.transform(), a.solid.sphereCenter);
		final Vector3 bc = transformPoint(b.transform(), b.solid.sphereCenter);

		final double radius = a.solid.sphereRadius + b.solid.sphereRadius;
		if (ac.distanceSquared(bc) > radius * radius) return;

		init(a);
		other.init(b);

		solid.collide(this);
	}

	private static Vector3 transformPoint(final Transform3 transform, final Vector3 point) {
		if (point == Vector3.Zero) return transform.offset;
		return transform.applyPoint(point);
	}

	boolean sphereTest() {
		final double radius = solid.sphereRadius + other.solid.sphereRadius;
		return solid.sphereCenter.distanceSquared(other.solid.sphereCenter) <= radius * radius;
	}

	void atoms() {
		final Solid a = solid, b = other.solid;

		// try previous separating axis if exists
		//contact = contacts.get(this);
		//if (contact != null && contact.dist > 0) candidate(contact.normal, null, null);

		other.contactDist = contactDist = Float.NEGATIVE_INFINITY;
		other.contactNormal = contactNormal = null;
		other.contactPoint = contactPoint = null;
		other.delta = delta = null;

		if (a instanceof Sphere)
			if (b instanceof Sphere)
				sphere2sphere((Sphere) a, (Sphere) b);
			else if (b instanceof Cylinder)
				other.cylinder2sphere((Cylinder) b, (Sphere) a);
			else if (b instanceof Box)
				other.box2sphere((Box) b, (Sphere) a);
			else if (b instanceof Polyhedron)
				other.poly2sphere((Polyhedron) b, (Sphere) a);
			else if (b instanceof Plane)
				other.plane2sphere((Plane) b, (Sphere) a);
			else
				throw new RuntimeException();
		else if (a instanceof Cylinder)
			if (b instanceof Sphere)
				cylinder2sphere((Cylinder) a, (Sphere) b);
			else if (b instanceof Cylinder)
				cylinder2cylinder((Cylinder) a, (Cylinder) b);
			else if (b instanceof Box)
				other.box2cylinder((Box) b, (Cylinder) a);
			else if (b instanceof Polyhedron)
				other.poly2cylinder((Polyhedron) b, (Cylinder) a);
			else if (b instanceof Plane)
				other.plane2cylinder((Plane) b, (Cylinder) a);
			else
				throw new RuntimeException();
		else if (a instanceof Box)
			if (b instanceof Sphere)
				box2sphere((Box) a, (Sphere) b);
			else if (b instanceof Cylinder)
				box2cylinder((Box) a, (Cylinder) b);
			else if (b instanceof Box)
				box2box((Box) a, (Box) b);
			else if (b instanceof Polyhedron)
				other.poly2box((Polyhedron) b, (Box) a);
			else if (b instanceof Plane)
				other.plane2box((Plane) b, (Box) a);
			else
				throw new RuntimeException();
		else if (a instanceof Polyhedron)
			if (b instanceof Sphere)
				poly2sphere((Polyhedron) a, (Sphere) b);
			else if (b instanceof Cylinder)
				poly2cylinder((Polyhedron) a, (Cylinder) b);
			else if (b instanceof Box)
				poly2box((Polyhedron) a, (Box) b);
			else if (b instanceof Polyhedron)
				poly2poly((Polyhedron) a, (Polyhedron) b);
			else if (b instanceof Plane)
				other.plane2poly((Plane) b, (Polyhedron) a);
			else
				throw new RuntimeException();
		else if (a instanceof Plane)
			if (b instanceof Sphere)
				plane2sphere((Plane) a, (Sphere) b);
			else if (b instanceof Cylinder)
				plane2cylinder((Plane) a, (Cylinder) b);
			else if (b instanceof Box)
				plane2box((Plane) a, (Box) b);
			else if (b instanceof Polyhedron)
				plane2poly((Plane) a, (Polyhedron) b);
			else
				throw new RuntimeException();
		else
			throw new RuntimeException();
	}

	private void sphere2sphere(final Sphere a, final Sphere b) {
		final Vector3 r = transform.offset.sub(other.transform.offset);

		contactDist = (float) r.length() - a.radius - b.radius;
		assert contactDist <= 0;

		contactNormal = r.unitz();
		// Case when centers of both spheres are the same point
		if (contactNormal == null) contactNormal = Vector3.X;

		contactPoint = other.transform.offset.add(b.radius + contactDist / 2, contactNormal);
		addContact();
	}

	private void cylinder2sphere(final Cylinder a, final Sphere b) {
		// Center of Sphere in Cylinder's local system
		final Vector3 p = other.transform.offset.sub(transform.offset);
		delta = p.mul(-2);

		final Vector3 na = transform.rotation.colZ();
		if (p.z > 0 ? candidate(na.neg(), p.z - a.halfHeight - b.radius) : candidate(na, -p.z - a.halfHeight
				- b.radius)) return;

		// Squared distance from Sphere's center to Cylinder's main axis
		final double ds = p.x * p.x + p.y * p.y;
		final double r = a.radius + b.radius;
		if (ds > r * r) return;

		Vector3 nb = Vector3.create(-p.x, -p.y, 0).unitz();
		if (nb == null) nb = Vector3.X;
		final double d = Math.sqrt(ds) - r;
		assert d <= 0;
		if (d > contactDist) {
			contactDist = (float) d;
			contactNormal = transform.applyVector(nb);
		}

		contactPoint = other.transform.offset.add(b.radius + contactDist / 2, contactNormal);
		addContact();
	}

	private void cylinder2cylinder(final Cylinder a, final Cylinder b) {
		final Vector3 e = transform.offset.sub(other.transform.offset);
		delta = e.mul(2);

		final Vector3 na = transform.rotation.colZ();
		if (candidateA(na, -a.halfHeight, a.halfHeight)) return;

		final Vector3 nb = other.transform.rotation.colZ();
		if (candidateB(nb, -b.halfHeight, b.halfHeight)) return;

		Vector3 n = na.unitzNormal(nb);
		// Case when cylinders are parallel
		if (n == null) {
			n = e.sub(na.dot(e) / na.square(), na).unitz();
			// Case when cylinders have common axis 
			if (n == null) n = Vector3.X;
		}
		if (candidate(n, -a.radius, a.radius, -b.radius, b.radius)) return;

		// FIXME contact point
		addContact();
	}

	private static Vector3 UnitXYZ = Vector3.unit(1, 1, 1);
	private static Vector3 UnitXY = Vector3.unit(1, 1, 0);
	private static Vector3 UnitXZ = Vector3.unit(1, 0, 1);
	private static Vector3 UnitYZ = Vector3.unit(0, 1, 1);

	private void box2sphere(final Box box, final Sphere b) {
		// sphere's center in box local coordinates
		final Vector3 p = transform.iapplyPoint(other.transform.offset);

		final float px = Math.abs(p.x) - box.x;
		final float py = Math.abs(p.y) - box.y;
		final float pz = Math.abs(p.z) - box.z;

		Vector3 axis = null;
		if (px > 0)
			if (py > 0)
				if (pz > 0)
					// vertex
					axis = Vector3.unit(px, py, pz, 1e-12f, UnitXYZ);
				else
					// edge z
					axis = Vector3.unit(px, py, 0, 1e-12f, UnitXY);
			else if (pz > 0)
				// edge y
				axis = Vector3.unit(px, 0, pz, 1e-12f, UnitXZ);
			else
				// face x
				axis = Vector3.X;
		else if (py > 0)
			if (pz > 0)
				// edge x
				axis = Vector3.unit(0, py, pz, 1e-12f, UnitYZ);
			else
				// face y
				axis = Vector3.Y;
		else if (pz > 0)
			// face z 
			axis = Vector3.Z;
		else // inside
		if (px > py && px > pz)
			axis = Vector3.X;
		else if (py > pz)
			axis = Vector3.Y;
		else
			axis = Vector3.Z;

		contactDist = axis.x * px + axis.y * py + axis.z * pz - b.radius;
		if (contactDist > 0) return;

		contactNormal = transform.applyVector(Vector3.create(axis.x * Math.signum(p.x), axis.y * Math.signum(p.y),
				axis.z * Math.signum(p.z)));

		contactPoint = other.transform.offset.add(b.radius + contactDist / 2, contactNormal);
		addContact();
	}

	private void box2cylinder(@SuppressWarnings("unused") final Box a, @SuppressWarnings("unused") final Cylinder b) {
		delta = transform.offset.sub(other.transform.offset).mul(2);

		// 1 cylinder axis
		// 1 cylinder radius

		// TODO box2cylinder
		throw new UnsupportedOperationException();
	}

	private void box2box(final Box a, final Box b) {
		delta = transform.offset.sub(other.transform.offset).mul(2);

		final Vector3[] an = new Vector3[3];
		if (candidateA(an[0] = transform.rotation.colX(), -a.x, a.x)) return;
		if (candidateA(an[1] = transform.rotation.colY(), -a.y, a.y)) return;
		if (candidateA(an[2] = transform.rotation.colZ(), -a.z, a.z)) return;

		final Vector3[] bn = new Vector3[3];
		if (candidateB(bn[0] = other.transform.rotation.colX(), -b.x, -b.x)) return;
		if (candidateB(bn[1] = other.transform.rotation.colY(), -b.y, -b.y)) return;
		if (candidateB(bn[2] = other.transform.rotation.colZ(), -b.z, -b.z)) return;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				final Vector3 n = an[i].unitzNormal(bn[i]);
				if (n != null && candidate(n)) return;
			}

		contactPoint = Vector3.average(intersectBoxBox(a, b));
		addContact();
	}

	private Vector3[] intersectBoxBox(final Box a, final Box b) {
		final Transform3 relative = transform.icombine(other.transform);

		tempBoxSetSize = BoxVertices;
		tempBoxSet = new Vector3[BoxVertices + BoxEdges * 4]; // NOTE can this be lowered?

		for (int i = 0; i < BoxVertices; i++)
			tempBoxSet[i] = relative.applyPoint(a.vertices[i]);
		for (int i = 0; i < BoxEdges; i++)
			boxClip(b, tempBoxSet[BoxEdgeA[i]], tempBoxSet[BoxEdgeB[i]], other.transform);

		for (int i = 0; i < BoxVertices; i++)
			tempBoxSet[i] = relative.iapplyPoint(b.vertices[i]);
		for (int i = 0; i < BoxEdges; i++)
			boxClip(a, tempBoxSet[BoxEdgeA[i]], tempBoxSet[BoxEdgeB[i]], transform);

		final Vector3[] v = new Vector3[tempBoxSetSize - BoxVertices];
		System.arraycopy(tempBoxSet, BoxVertices, v, 0, tempBoxSetSize - BoxVertices);
		return v;
	}

	private final static int BoxVertices = 8, BoxEdges = 12;
	private final static int[] BoxEdgeA = { 0, 2, 0, 1, 4, 6, 4, 5, 0, 2, 1, 3 };
	private final static int[] BoxEdgeB = { 1, 3, 2, 3, 5, 7, 6, 7, 4, 6, 5, 7 };

	private Vector3[] tempBoxSet;
	private int tempBoxSetSize;

	private void boxClip(final Box q, Vector3 a, Vector3 b, final Transform3 tr) {
		final float eps = 0;

		// clipX
		if (a.x > b.x) {
			final Vector3 t = a;
			a = b;
			b = t;
		}

		if (b.x < -q.x - eps || a.x > q.x + Side.eps) return;
		if (a.x < -q.x - eps) a = Vector3.linear(a, b, (-q.x - a.x) / (b.x - a.x));
		if (b.x > q.x + eps) b = Vector3.linear(a, b, (q.x - a.x) / (b.x - a.x));

		// clipY
		if (a.y > b.y) {
			final Vector3 t = a;
			a = b;
			b = t;
		}

		if (b.y < -q.y - eps || a.y > q.y + eps) return;
		if (a.y < -q.y - eps) a = Vector3.linear(a, b, (-q.y - a.y) / (b.y - a.y));
		if (b.y > q.y + eps) b = Vector3.linear(a, b, (q.y - a.y) / (b.y - a.y));

		// clipZ
		if (a.z > b.z) {
			final Vector3 t = a;
			a = b;
			b = t;
		}

		if (b.z < -q.z - eps || a.z > q.z + eps) return;
		if (a.z < -q.z - eps) a = Vector3.linear(a, b, (-q.z - a.z) / (b.z - a.z));
		if (b.z > q.z + eps) b = Vector3.linear(a, b, (q.z - a.z) / (b.z - a.z));

		// finish
		if (!a.equals(b)) boxSetAdd(tr.applyPoint(b));
		boxSetAdd(tr.applyPoint(a));
	}

	private void boxSetAdd(final Vector3 a) {
		for (int i = BoxVertices; i < tempBoxSetSize; i++)
			if (tempBoxSet[i].equals(a)) return;
		tempBoxSet[tempBoxSetSize++] = a;
	}

	private void poly2sphere(final Polyhedron a, final Sphere b) {
		delta = transform.offset.sub(other.transform.offset).mul(2);
		// TODO optimize poly2sphere

		// Poly Faces
		for (int i = 0; i < a.faces.length; i++)
			if (candidate(transform.applyVector(a.faces[i].plane.unitNormal), a.intervals[i].min,
					a.intervals[i].max, -b.radius, b.radius)) return;

		// Poly Edges
		final Vector3 p = transform.iapplyPoint(other.transform.offset);
		for (final Line3 e : a.edges) {
			Vector3 n = e.normalInf(p);
			if (n == null) n = e.normalInf(Vector3.Zero).neg();
			final Interval ia = a.interval(n);
			if (candidate(transform.applyVector(n), ia.min, ia.max, -b.radius, b.radius)) return;
		}

		// Poly Vertices
		for (final Vector3 v : a.vertices) {
			Vector3 n = p.sub(v).unitz();
			if (n == null) n = v.unitz();
			final Interval ia = a.interval(n);
			if (candidate(transform.applyVector(n), ia.min, ia.max, -b.radius, b.radius)) return;
		}

		contactPoint = other.transform.offset.add(b.radius + contactDist / 2, contactNormal);
		addContact();
	}

	private void poly2cylinder(@SuppressWarnings("unused") final Polyhedron a,
			@SuppressWarnings("unused") final Cylinder b) {
		// TODO poly2cylinder
		throw new UnsupportedOperationException();
	}

	private void poly2box(final Polyhedron a, final Box b) {
		delta = transform.offset.sub(other.transform.offset).mul(2);

		// A.faces
		for (int i = 0; i < a.faces.length; i++)
			if (candidateA(transform.applyVector(a.faces[i].plane.unitNormal), a.intervals[i].min,
					a.intervals[i].max)) return;

		final Vector3[] bn = new Vector3[3];
		if (candidateB(bn[0] = other.transform.rotation.colX(), -b.x, b.x)) return;
		if (candidateB(bn[1] = other.transform.rotation.colY(), -b.y, b.y)) return;
		if (candidateB(bn[2] = other.transform.rotation.colZ(), -b.z, b.z)) return;

		for (final Line3 ea : a.edges) {
			final Vector3 da = transform.applyVector(ea.direction());
			for (int j = 0; j < 3; j++) {
				final Vector3 db = other.transform.applyVector(bn[j]);
				final Vector3 n = da.unitzNormal(db);
				if (n != null && candidate(n)) return;
			}
		}

		// TODO optimize!
		contactPoint = intersectPolyPoly(a, b.polyhedron);
		addContact();
	}

	private void poly2poly(final Polyhedron a, final Polyhedron b) {
		delta = transform.offset.sub(other.transform.offset).mul(2);

		// A.faces * B.vertices
		for (int i = 0; i < a.faces.length; i++)
			if (candidateA(transform.applyVector(a.faces[i].plane.unitNormal), a.intervals[i].min,
					a.intervals[i].max)) return;

		// B.faces * A.vertices
		for (int i = 0; i < b.faces.length; i++)
			if (candidateB(other.transform.applyVector(b.faces[i].plane.unitNormal), b.intervals[i].min,
					b.intervals[i].max)) return;

		// A.edges * B.edges * (A.vertices + B.vertices)
		final Vector3[] dbList = new Vector3[b.edges.length];
		for (int i = 0; i < dbList.length; i++)
			dbList[i] = other.transform.applyVector(b.edges[i].direction());

		for (final Line3 ea : a.edges) {
			final Vector3 da = transform.applyVector(ea.direction());
			for (final Vector3 db : dbList) {
				final Vector3 n = da.unitzNormal(db);
				if (n != null && candidate(n)) return;
			}
		}

		contactPoint = intersectPolyPoly(a, b);
		addContact();
	}

	private Vector3 intersectPolyPoly(final Polyhedron a, final Polyhedron b) {
		final List<Vector3> list = new ArrayList<Vector3>();

		final Transform3 relative = transform.icombine(other.transform);
		for (final Line3 e : a.edges) {
			final Line3 p = Geometry3.convexClip(b.faces, relative.apply(e), 0);
			if (p != null) {
				if (p.a != e.a) list.add(other.transform.applyPoint(p.a));
				if (p.b != p.a && p.b != e.b) list.add(other.transform.applyPoint(p.b));
			}
		}
		for (final Vector3 v : a.vertices)
			if (Polyhedrons.side(b.faces, relative.applyPoint(v), 0) != Side.Positive)
				list.add(transform.applyPoint(v));

		for (final Line3 e : b.edges) {
			final Line3 p = Geometry3.convexClip(a.faces, relative.iapply(e), 0);
			if (p != null) {
				if (p.a != e.a) list.add(transform.applyPoint(p.a));
				if (p.b != p.a && p.b != e.b) list.add(transform.applyPoint(p.b));
			}
		}
		for (final Vector3 v : b.vertices)
			if (Polyhedrons.side(a.faces, relative.iapplyPoint(v), 0) != Side.Positive)
				list.add(other.transform.applyPoint(v));

		assert list.size() > 0;
		return Vector3.average(list);
	}

	private void plane2sphere(final Plane a, final Sphere b) {
		assert transform == Transform3.Identity;
		final float dist = a.plane.distance(other.transform.offset) - b.radius;
		if (dist > 0) return;

		contactNormal = a.plane.unitNormal;
		contactDist = dist;
		contactPoint = other.transform.offset.add(b.radius + contactDist / 2, contactNormal);
		addContact();
	}

	private static float support(final Cylinder c, final Vector3 dir) {
		final float dz = dir.z * c.halfHeight;
		return (float) Math.sqrt(dz * dz + (dir.x * dir.x + dir.y * dir.y) * c.radius * c.radius);
	}

	private void plane2cylinder(final Plane a, final Cylinder b) {
		assert transform == Transform3.Identity;
		final float d = a.plane.distance(other.transform.offset);
		if (d > b.sphereRadius) return;

		final float dist = d - support(b, other.transform.iapplyVector(a.plane.unitNormal));
		if (dist > 0) return;

		contactNormal = a.plane.unitNormal;
		contactDist = dist;
		// FIXME contact point
		addContact();
	}

	private static float support(final Box b, final Vector3 dir) {
		return Math.abs(dir.x) * b.x + Math.abs(dir.y) * b.y + Math.abs(dir.z) * b.z;
	}

	private void plane2box(final Plane a, final Box b) {
		assert transform == Transform3.Identity;
		final float d = a.plane.distance(other.transform.offset);
		if (d > b.sphereRadius) return;

		final float dist = d - support(b, other.transform.iapplyVector(a.plane.unitNormal));
		if (dist > 0) return;

		contactNormal = a.plane.unitNormal;
		contactDist = dist;
		// TODO optimize
		contactPoint = intersectPlanePoly(a, b.polyhedron);
		addContact();
	}

	private void plane2poly(final Plane a, final Polyhedron b) {
		assert transform == Transform3.Identity;
		final float d = a.plane.distance(other.transform.offset);
		if (d > b.sphereRadius) return;

		final float dist = d + b.interval(other.transform.iapplyVector(a.plane.unitNormal)).min;
		if (dist > 0) return;

		contactNormal = a.plane.unitNormal;
		contactDist = dist;
		contactPoint = intersectPlanePoly(a, b);
		addContact();
	}

	private Vector3 intersectPlanePoly(final Plane a, final Polyhedron b) {
		assert transform == Transform3.Identity;
		final List<Vector3> list = new ArrayList<Vector3>();

		final Plane3 xplane = other.transform.iapply(a.plane);
		for (final Line3 e : b.edges) {
			final Line3 p = e.clip(xplane, 0);
			if (p != null) {
				if (p.a != e.a) list.add(other.transform.applyPoint(p.a));
				if (p.b != p.a) if (p.b != e.b) list.add(other.transform.applyPoint(p.b));
			}
		}
		for (final Vector3 v : b.vertices)
			if (xplane.distance(v) <= 0) list.add(other.transform.applyPoint(v));

		assert list.size() > 0;
		return Vector3.average(list);
	}

	private void addContact() {
		contacts.add(new Contact(body, other.body, material, other.material, contactNormal, contactPoint,
				-contactDist, Arbiter.get(body, other.body)));
	}

	private boolean candidate(final Vector3 normal) {
		assert normal != null;
		final Interval a = solid.interval(transform.iapplyVector(normal));
		final Interval b = other.solid.interval(other.transform.iapplyVector(normal));
		return candidate(normal, a.min, a.max, b.min, b.max);
	}

	private boolean candidateA(final Vector3 normal, final float amin, final float amax) {
		assert normal != null;
		final Interval b = other.solid.interval(other.transform.iapplyVector(normal));
		return candidate(normal, amin, amax, b.min, b.max);
	}

	private boolean candidateB(final Vector3 normal, final float bmin, final float bmax) {
		assert normal != null;
		final Interval a = solid.interval(transform.iapplyVector(normal));
		return candidate(normal, a.min, a.max, bmin, bmax);
	}

	private boolean candidate(Vector3 normal, final float amin, final float amax, final float bmin, final float bmax) {
		assert normal != null;

		float z = amin + amax - bmin - bmax + normal.dot(delta);
		if (z < 0) {
			z = -z;
			normal = normal.neg();
		}
		return candidate(normal, (z - (amax - amin + bmax - bmin)) / 2);
	}

	private boolean candidate(final Vector3 normal, final float dist) {
		assert normal != null;
		assert Math.abs(normal.square() - 1) <= 1e-2 : normal.toString() + " " + Math.abs(normal.square() - 1);

		if (dist <= contactDist) return false;
		contactDist = dist;
		contactNormal = normal;
		return dist > 0;
	}
}
