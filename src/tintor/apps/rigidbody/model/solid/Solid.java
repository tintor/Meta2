package tintor.apps.rigidbody.model.solid;

import java.util.Arrays;
import java.util.List;

import tintor.geometry.Interval;
import tintor.geometry.Matrix3;
import tintor.geometry.Plane3;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;

public abstract class Solid {
	public Vector3 sphereCenter;
	public float sphereRadius;

	public abstract Vector3 centerOfMass();

	public abstract float mass();

	public abstract Matrix3 inertiaTensor();

	public abstract float maximal(Vector3 center);

	// Negative distance if point is inside
	public abstract float distance(Vector3 point);

	public abstract void render();

	public abstract void collide(final Collision pair);

	public Interval interval(@SuppressWarnings("unused") final Vector3 normal) {
		throw new IllegalStateException();
	}

	// -----------

	public final static Point Point = new Point();

	public static Solid cube(final float a) {
		return new Box(a, a, a);
	}

	public static Solid box(final float a, final float b, final float c) {
		return new Box(a, b, c);
	}

	public static Solid composite(final Solid... list) {
		return new Composite(Arrays.asList(list));
	}

	public static Solid composite(final List<Solid> list) {
		return new Composite(list);
	}

	public static Solid cylinder(final float radius, final float a) {
		return new Cylinder(radius, a);
	}

	public static Solid plane(final Plane3 plane) {
		return new Plane(plane);
	}

	public static Solid convex(final Vector3... vertices) {
		return new Polyhedron(Arrays.asList(vertices));
	}

	public static Solid convex(final List<Vector3> vertices) {
		return new Polyhedron(vertices);
	}

	public static Solid sphere(final float radius) {
		return new Sphere(radius);
	}

	// -----------

	public Solid compile() {
		return new Compiled(this);
	}

	public Solid transform(final Transform3 transform) {
		return new Transformed(this, transform);
	}

	public Solid density(final float density) {
		return new Density(this, density);
	}

	public Solid friction(final float sfriction, final float dfriction) {
		final Material m = new Material(this);
		m.sfriction = sfriction;
		m.dfriction = dfriction;
		return m;
	}

	public Solid elasticity(final float elasticity) {
		final Material m = new Material(this);
		m.elasticity = elasticity;
		return m;
	}

	public Solid drag(final float drag) {
		final Material m = new Material(this);
		m.drag = drag;
		return m;
	}

	public Solid color(final Vector3 color) {
		return new Colored(this, color);
	}

	public Solid color(final float red, final float green, final float blue) {
		return new Colored(this, Vector3.create(red, green, blue));
	}
}