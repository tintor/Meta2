package tintor.apps.rigidbody.main.worlds;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.effector.Drag;
import tintor.apps.rigidbody.model.solid.Solid;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Stack extends World {
	final Solid cube = Solid.cube(2).friction(0.5f, 0.5f).elasticity(0);
	Solid blueCube = cube.color(GLA.blue).compile();
	Solid mangentaCube = cube.color(GLA.mangenta).compile();

	public Stack() {
		stack(-30, 10);
		stack(0, 15);
		stack(30, 25);

		add(new Drag());
		surface(-8, 5);
	}

	void stack(final double x, final int n) {
		double h = 5, y = -20;
		for (int i = 0; i < n; i++) {
			y += 4.1;
			final Body a = new Body(Vector3.create(x, y + 10, (Math.random() - 0.5) / 1000000),
					Quaternion.Identity, i % 2 == 0 ? mangentaCube : blueCube);
			a.name = "big";
			add(a);
			h -= 0.5;
		}
	}
}