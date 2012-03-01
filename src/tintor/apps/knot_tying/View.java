package tintor.apps.knot_tying;

import javax.media.opengl.GL;

import tintor.apps.rigidbody.tools.GLA;

public class View {
	// Fields
	public Projection projection = new Projection();

	public boolean wireframe = false;
	public boolean contacts = false;
	public boolean system = false;
	public boolean renderText = false;

	public boolean saveScreen = false;

	int systemList;

	public void init() {
		GLA.enable(GL.GL_DEPTH_TEST);
		GLA.gl.glCullFace(GL.GL_BACK);
		GLA.gl.glClearColor(0, 0, 0, 1);

		GLA.gl.glShadeModel(GL.GL_SMOOTH);

		final int size = 10;
		systemList = GLA.gl.glGenLists(1);
		GLA.gl.glNewList(systemList, GL.GL_COMPILE);
		GLA.gl.glBegin(GL.GL_LINES);
		for (int i = 0; i < size; i++) {
			// XY
			GLA.color(GLA.red);
			GLA.vertex(0, i, 0);
			GLA.vertex(size, i, 0);

			GLA.color(GLA.green);
			GLA.vertex(i, 0, 0);
			GLA.vertex(i, size, 0);

			// XZ
			GLA.color(GLA.red);
			GLA.vertex(0, 0, i);
			GLA.vertex(size, 0, i);

			GLA.color(GLA.blue);
			GLA.vertex(i, 0, 0);
			GLA.vertex(i, 0, size);

			// YZ
			GLA.color(GLA.green);
			GLA.vertex(0, 0, i);
			GLA.vertex(0, size, i);

			GLA.color(GLA.blue);
			GLA.vertex(0, i, 0);
			GLA.vertex(0, i, size);
		}
		GLA.gl.glEnd();

		GLA.gl.glEndList();
	}

	public void display() {
		// setup
		GLA.gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);

		// render
	}

	public void reshape(@SuppressWarnings("unused") final int x, @SuppressWarnings("unused") final int y,
			final int width, final int height) {
		projection.aspectRatio = (float) width / (float) height;
		projection.setMatrix();
	}
}