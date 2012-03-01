package tintor.apps.rigidbody.model.solid;

import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;

class Decorator extends Solid {
	final Solid solid;

	Decorator(final Solid s) {
		solid = s;
		sphereCenter = solid.sphereCenter;
		sphereRadius = solid.sphereRadius;
	}

	@Override
	public Vector3 centerOfMass() {
		return solid.centerOfMass();
	}

	@Override
	public float mass() {
		return solid.mass();
	}

	@Override
	public Matrix3 inertiaTensor() {
		return solid.inertiaTensor();
	}

	@Override
	public float maximal(final Vector3 center) {
		return solid.maximal(center);
	}

	@Override
	public float distance(final Vector3 point) {
		return solid.distance(point);
	}

	@Override
	public void render() {
		solid.render();
	}

	@Override
	public void collide(final Collision pair) {
		pair.solid = solid;
		solid.collide(pair);
	}

	//	@Override
	//	public void findContacts(final CollisionPair pair) {
	//		pair.solidA = solid;
	//		solid.findContacts(pair);
	//		pair.solidA = this;
	//	}

	//	@Override
	//	public Vector3[] intersection(final Plane3 plane) {
	//		return solid.intersection(plane);
	//	}

	//	@Override
	//	public Convex convexHull() {
	//		return solid.convexHull();
	//	}
}