package tintor.geometry;

import java.util.concurrent.atomic.AtomicInteger;

public final class Transform3 {
	private static final AtomicInteger counter = new AtomicInteger();

	/** @return number of objects constructed */
	public static int counter() {
		return counter.get();
	}

	// Constants
	/** Identity transform: I.apply(X) = X */
	public final static Transform3 Identity = new Transform3(Matrix3.Identity, Vector3.Zero);

	// Factory Methods
	public static Transform3 translation(final Vector3 offset) {
		return new Transform3(Matrix3.Identity, offset);
	}

	public static Transform3 rotation(final Vector3 axis, final float angleRadians) {
		return create(Quaternion.axisAngle(axis, angleRadians), Vector3.Zero);
	}

	public static Transform3 create(final Quaternion rotation, final Vector3 offset) {
		if (offset == null) throw new IllegalArgumentException();
		return new Transform3(rotation.matrix(), offset);
	}

	// Fields
	/** matrix describing rotational part of transform */
	public final Matrix3 rotation;
	/** vector describing translational part of transform */
	public final Vector3 offset;
	private final Vector3 invOffset; // = - offset * rotation

	// Constructors
	/** ASSUME m is rotational! */
	private Transform3(final Matrix3 rotation, final Vector3 offset) {
		assert rotation != null;
		assert offset != null;

		this.rotation = rotation;
		this.offset = offset;

		final float nx = offset.x * rotation.a.x + offset.y * rotation.b.x + offset.z * rotation.c.x;
		final float ny = offset.x * rotation.a.y + offset.y * rotation.b.y + offset.z * rotation.c.y;
		final float nz = offset.x * rotation.a.z + offset.y * rotation.b.z + offset.z * rotation.c.z;
		invOffset = Vector3.create(-nx, -ny, -nz);

		counter.incrementAndGet();
	}

	// Direct Transformations
	public Vector3 applyPoint(final Vector3 point) {
		return offset.add(rotation, point);
	}

	public Vector3 applyVector(final Vector3 vector) {
		return rotation.mul(vector);
	}

	public Plane3 apply(final Plane3 plane) {
		final Vector3 normal = applyVector(plane.unitNormal);
		return Plane3.create(normal, plane.offset - offset.dot(normal));
	}

	public Ray3 apply(final Ray3 ray) {
		return Ray3.ray(applyPoint(ray.origin), applyVector(ray.unitDir));
	}

	public Line3 apply(final Line3 line) {
		return Line3.create(applyPoint(line.a), applyPoint(line.b));
	}

	// Inverse Transformations
	public Vector3 iapplyPoint(final Vector3 point) {
		return invOffset.add(point, rotation);
	}

	public Vector3 iapplyVector(final Vector3 vector) {
		return vector.mul(rotation);
	}

	public Plane3 iapply(final Plane3 plane) {
		return Plane3.create(iapplyVector(plane.unitNormal), plane.offset + offset.dot(plane.unitNormal));
	}

	public Ray3 iapply(final Ray3 ray) {
		return Ray3.ray(iapplyPoint(ray.origin), iapplyVector(ray.unitDir));
	}

	public Line3 iapply(final Line3 line) {
		return Line3.create(iapplyPoint(line.a), iapplyPoint(line.b));
	}

	// Misc
	public Transform3 combine(final Transform3 a) {
		return new Transform3(a.rotation.mul(rotation), mulAdd(a.rotation, offset, a.offset));
	}

	/** @return A * b + c */
	private static Vector3 mulAdd(final Matrix3 a, final Vector3 b, final Vector3 c) {
		return Vector3.create(a.a.dot(b) + c.x, a.b.dot(b) + c.y, a.c.dot(b) + c.z);
	}

	public Transform3 icombine(final Transform3 a) {
		return new Transform3(a.rotation.transposedMul(rotation), subMul(offset, a.offset, a.rotation));
	}

	/** @return (a - b) * C */
	private static Vector3 subMul(final Vector3 a, final Vector3 b, final Matrix3 c) {
		final float x = a.x - b.x, y = a.y - b.y, z = a.z - b.z;
		final float nx = x * c.a.x + y * c.b.x + z * c.c.x;
		final float ny = x * c.a.y + y * c.b.y + z * c.c.y;
		final float nz = x * c.a.z + y * c.b.z + z * c.c.z;
		return Vector3.create(nx, ny, nz);
	}

	public Transform3 inverse() {
		// for rotational matrices, inverse(M) = transpose(M)
		// inv(M,V) = (invM, -invM*V) = (transposeM, -V*M)
		return new Transform3(rotation.transpose(), invOffset);
	}
}