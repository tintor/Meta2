package tintor.apps.knot_tying;

import tintor.geometry.Line3;
import tintor.geometry.Vector3;

public class Rope {
	private final static boolean capsuleCollisionModel = false;

	private int nodes;
	private final Vector3[] node;
	private final Vector3[] responce;
	private final int[] matches;

	private final SpatialHashTable hash;

	public final float radius, linkLength;
	private final int minCollisionDistance;

	private final float squaredRadius4;
	private final float squaredRadiusFuzz;
	private float responceOffset;

	private float speed = 0.1f, separation = 0.1f;

	public Rope(final int capacity, final float radius, final float linkLength) {
		node = new Vector3[capacity];
		responce = new Vector3[capacity];
		matches = new int[capacity - 1];
		hash = new SpatialHashTable(16, (radius + linkLength) * 0.75f);

		this.radius = radius;
		this.linkLength = linkLength;
		minCollisionDistance = Math.max(1, (int) Math.floor(2 * radius / linkLength));

		squaredRadius4 = radius * radius * 4;
		squaredRadiusFuzz = radius * radius * 1e-6f;
	}

	// distance relative to radius
	public void setSpeed(final float speed) {
		if (speed < 0.01f || speed > 0.4f)
			throw new IllegalArgumentException();
		this.speed = speed;
	}

	// distance relative to radius
	public void setSeparation(final float separation) {
		if (separation < 0.01f || separation > 0.4f)
			throw new IllegalArgumentException();
		this.separation = separation;
	}

	public float lengthError() {
		float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
		for (int i = 1; i < nodes; i++) {
			final float d = node[i].distanceSquared(node[i - 1]);
			if (d < min) {
				min = d;
			}
			if (d > max) {
				max = d;
			}
		}
		max /= linkLength * linkLength;
		min /= linkLength * linkLength;
		return Math.max(Math.abs(1 - (float) Math.sqrt(max)), Math.abs(1 - (float) Math.sqrt(min)));
	}

	// @returns actual position of node or null if it would cause self-intersection
	public Vector3 extend(Vector3 a) {
		if (nodes >= node.length)
			throw new RuntimeException("node capacity overflow");

		if (nodes > 0) {
			final Vector3 b = node[nodes - 1];
			a = a.sub(b).limitLength(linkLength).add(b);
		}
		node[nodes] = a;

		if (findCollisionsWith(nodes))
			return null;

		nodes += 1;
		return a;
	}

	public void move(final int index, final Vector3 dest) {
		if (index < 0 || index >= nodes)
			throw new IndexOutOfBoundsException();

		// shift the leader
		shift(index, dest);

		// follow the leader
		for (int i = index + 1; i < nodes; i++) {
			follow(i, node[i - 1]);
		}
		for (int i = index - 1; i >= 0; i--) {
			follow(i, node[i + 1]);
		}

		// find self-collisions
		hash.clear();
		responceOffset = (1 + separation / 2) * radius;
		for (int a = 0; a < nodes; a++) {
			findCollisionsWith(a);
		}

		// apply collision responses 
		for (int a = 0; a < nodes; a++) {
			applyCollisionsTo(a);
		}
	}

	private void applyCollisionsTo(final int a) {
		shift(a, capsuleCollisionModel && a + 1 < nodes ? responce[a].add(responce[a + 1]) : responce[a]);
	}

	private boolean findCollisionsWith(final int a) {
		if (capsuleCollisionModel && a == 0)
			return false;

		boolean collisions = false;
		final Vector3 key = capsuleCollisionModel ? Vector3.average(node[a], node[a - 1]) : node[a];

		responce[a] = Vector3.Zero;
		final int results = hash.lookup(key, matches);
		for (int i = 0; i < results; i++) {
			final int linkB = matches[i];
			if (Math.abs(linkB - a) >= minCollisionDistance) {
				if (collide(a, linkB)) {
					collisions = true;
				}
			}
		}

		hash.add(key, a);
		return collisions;
	}

	private boolean collide(final int a, final int b) {
		Vector3 A, B;
		if (capsuleCollisionModel) {
			final Line3 lineA = Line3.create(node[a], node[a - 1]);
			final Line3 lineB = Line3.create(node[b], node[b - 1]);
			final Line3.Result result = lineA.nearest(lineB);
			A = lineA.point(result.a);
			B = lineB.point(result.b);
		} else {
			A = node[a];
			B = node[b];
		}

		final float ds = A.distanceSquared(B);
		if (ds >= squaredRadius4)
			return false;
		if (ds < squaredRadiusFuzz)
			throw new RuntimeException("Illegal collision state");

		final float offset = responceOffset / (float) Math.sqrt(ds) - 0.5f;
		final Vector3 delta = A.sub(B);
		responce[a] = responce[a].add(offset, delta);
		responce[b] = responce[b].add(-offset, delta);
		return true;
	}

	private void follow(final int index, final Vector3 leader) {
		final Vector3 delta = leader.sub(node[index]);
		shift(index, delta.mul(1 - linkLength / (float) delta.length()));
	}

	private void shift(final int index, final Vector3 delta) {
		node[index] = node[index].add(delta.limitLength(speed * radius));
	}
}