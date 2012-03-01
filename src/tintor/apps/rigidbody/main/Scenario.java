package tintor.apps.rigidbody.main;

import tintor.apps.rigidbody.model.World;

public abstract class Scenario {
	protected abstract void init(World world);

	public void keyDown(@SuppressWarnings("unused") final int key) {
	}

	public void keyUp(@SuppressWarnings("unused") final int key) {
	}
}
