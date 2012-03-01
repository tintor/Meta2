package tintor.apps.rigidbody.model.solid;

import tintor.geometry.Vector3;

abstract class Convex extends Solid {
	Convex() {
		sphereCenter = Vector3.Zero;
	}

	@Override
	public Vector3 centerOfMass() {
		return Vector3.Zero;
	}

	@Override
	public void collide(final Collision pair) {
		if (pair.other.solid instanceof Convex)
			pair.atoms();
		else
			pair.other.solid.collide(pair.other);
	}
}