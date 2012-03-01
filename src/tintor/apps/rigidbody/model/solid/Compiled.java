package tintor.apps.rigidbody.model.solid;

import javax.media.opengl.GL;

import tintor.apps.rigidbody.tools.GLA;

final class Compiled extends Decorator {
	private int displayList;
	private boolean created;

	Compiled(final Solid solid) {
		super(solid);

	}

	@Override
	public void finalize() {
		if (created) GLA.gl.glDeleteLists(displayList, 1);
	}

	@Override
	public void render() {
		if (!created) {
			displayList = GLA.gl.glGenLists(1);
			GLA.gl.glNewList(displayList, GL.GL_COMPILE);
			solid.render();
			GLA.gl.glEndList();
			created = true;
		}
		GLA.gl.glCallList(displayList);
	}

	@Override
	public Compiled compile() {
		return this;
	}
}