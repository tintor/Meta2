package tintor.apps.rigidbody.model.solid;

public class Material extends Decorator {
	public float elasticity = 0.25f;
	public float drag = 0.003f;
	public float sfriction = 0.25f, dfriction = 0.2f;

	public Material(final Solid solid) {
		super(solid);
	}

	@Override
	public void collide(final Collision pair) {
		pair.material = this;
		super.collide(pair);
	}

	@Override
	public Solid friction(final float sfric, final float dfric) {
		sfriction = sfric;
		dfriction = dfric;
		return this;
	}

	@Override
	public Solid elasticity(final float elas) {
		elasticity = elas;
		return this;
	}

	@Override
	public Solid drag(final float drg) {
		drag = drg;
		return this;
	}
}
