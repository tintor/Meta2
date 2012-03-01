package tintor.apps.rigidbody.model.solid;

import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Matrix3;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;

public final class Transformed extends Decorator {
	public final Transform3 transform;

	public Transformed(final Solid s, final Transform3 transform) {
		super(s);
		this.transform = transform;
		sphereCenter = transform.applyPoint(solid.sphereCenter);
	}

	@Override
	public Vector3 centerOfMass() {
		return transform.applyPoint(solid.centerOfMass());
	}

	@Override
	public Matrix3 inertiaTensor() {
		return transform.rotation.mulTransposed(solid.inertiaTensor()).mul(transform.rotation).add(
				transform.offset.tildaSqr(), -mass());
	}

	@Override
	public float distance(final Vector3 point) {
		return solid.distance(transform.iapplyPoint(point));
	}

	@Override
	public float maximal(final Vector3 center) {
		return solid.maximal(transform.iapplyPoint(center));
	}

	@Override
	public void render() {
		GLA.gl.glPushMatrix();
		GLA.multMatrix(transform);
		solid.render();
		GLA.gl.glPopMatrix();
	}

	@Override
	public Transformed transform(final Transform3 a) {
		return new Transformed(solid, transform.combine(a));
	}

	@Override
	public void collide(final Collision pair) {
		final Transform3 p = pair.transform;
		// TODO optimize: this line will be executed multiple times during single narrow detection phase
		pair.transform = transform.combine(pair.transform);
		super.collide(pair);
		pair.transform = p;
	}
}