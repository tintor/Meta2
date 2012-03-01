package tintor.apps.rigidbody.tools;

import java.util.Arrays;
import java.util.Random;

import tintor.geometry.ConvexHull3;
import tintor.geometry.Polygon3;
import tintor.geometry.Vector3;

public abstract class ConvexPolyhedrons {
	/** golden ratio */
	private static final float fi = (float) ((1 + Math.sqrt(5)) / 2);

	public static Polygon3[] make(final Vector3... vertices) {
		return new ConvexHull3(Arrays.asList(vertices), true).visit(Polyhedrons.createBuilder());
	}

	public static VList sphere(final float radius, final int segments) {
		final VList v = new VList().add(0, radius, 0).add(0, -radius, 0);

		final float slice = (float) (Math.PI / segments);
		for (int a = 1; a < segments; a++) {
			final float cosA = (float) Math.cos(a * slice) * radius;
			final float sinA = (float) Math.sin(a * slice) * radius;
			for (int b = 0; b < segments * 2; b++) {
				final float cosB = (float) Math.cos(b * slice);
				final float sinB = (float) Math.sin(b * slice);
				v.add(sinA * cosB, cosA, sinA * sinB);
			}
		}
		return v;
	}

	public static VList randomSphere(final float radius, final int vertices) {
		final VList v = new VList();
		final Random rand = new Random();
		for (int i = 0; i < vertices; i++)
			v.add(Vector3.randomDirection(rand).mul(radius));
		return v;
	}

	public static VList truncatedCube() {
		return new VList().addc3((float) Math.sqrt(2) - 1, 1, 1);
	}

	public static VList football() {
		return new VList().addc2(0, 1, 3 * fi).addc3(2, 1 + 2 * fi, fi).addc3(1, 2 + fi, 2 * fi);
	}

	public static VList tetrahedron() {
		return new VList().add(1, 1, 1).addc(-1, -1, 1);
	}

	/** Half edges */
	public static VList cube(final float x, final float y, final float z) {
		return new VList().add3(x, y, z);
	}

	public static VList octahedron(final float r) {
		return new VList().addc(r, 0, 0).addc(-r, 0, 0);
	}

	public static VList dodecahedron() {
		return new VList().add3(1, 1, 1).addc2(0, 1 / fi, fi);
	}

	public static VList icosahedron() {
		return new VList().addc2(0, 1, fi);
	}

	private static Vector3[] polygon(final int k, final float semiradius) {
		final Vector3[] v = new Vector3[k];
		final float a = (float) (Math.PI * 2 / k);
		for (int i = 0; i < k; i++)
			v[i] = Vector3.mul((float) Math.cos(a * i), (float) Math.sin(a * i), 0, semiradius);
		return v;
	}

	public static VList prism(final int k, final float r1, final float r2, final float height) {
		return prism(polygon(k, r1), polygon(k, r2), height);
	}

	public static VList prism(final Vector3[] poly1, final Vector3[] poly2, final float height) {
		final VList v = new VList();
		for (final Vector3 a : poly1)
			v.add(a.x, a.y, height / 2);
		for (final Vector3 a : poly2)
			v.add(a.x, a.y, -height / 2);
		return v;
	}

	public static VList pyramid(final int k, final float r, final float height) {
		return pyramid(polygon(k, r), height);
	}

	public static VList pyramid(final Vector3[] poly, final float height) {
		final VList v = new VList().add(0, 0, height * 2.0f / 3);
		for (final Vector3 a : poly)
			v.add(a.x, a.y, -height / 3);
		return v;
	}
}