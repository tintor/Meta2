package tintor.apps.rigidbody.model.solid;

import tintor.geometry.Matrix3;

final class Density extends Decorator {
	public final float density;

	Density(final Solid shape, final float density) {
		super(shape);
		this.density = density;
	}

	@Override
	public float mass() {
		return solid.mass() * density;
	}

	@Override
	public Matrix3 inertiaTensor() {
		return solid.inertiaTensor().mul(density);
	}

	@Override
	public Solid density(final float densityM) {
		return new Density(solid, density * densityM);
	}
}