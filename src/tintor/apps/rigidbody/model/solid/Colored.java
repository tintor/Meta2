package tintor.apps.rigidbody.model.solid;

import javax.media.opengl.GL;

import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Vector3;

final class Colored extends Decorator {
	Vector3 color;

	Colored(final Solid shape, final Vector3 color) {
		super(shape);
		this.color = color;
	}

	@Override
	public void render() {
		GLA.gl.glPushAttrib(GL.GL_CURRENT_BIT);
		GLA.color(color);
		super.render();
		GLA.gl.glPopAttrib();
	}
}