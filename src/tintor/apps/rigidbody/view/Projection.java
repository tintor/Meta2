package tintor.apps.rigidbody.view;

import javax.media.opengl.GL;

import tintor.apps.rigidbody.tools.GLA;

public class Projection {
	public boolean perspective = true;
	public float aspectRatio = 1;

	public void setMatrix() {
		GLA.gl.glMatrixMode(GL.GL_PROJECTION);
		GLA.gl.glLoadIdentity();
		rawMatrix();
		GLA.gl.glMatrixMode(GL.GL_MODELVIEW);
	}

	public void rawMatrix() {
		float w = aspectRatio > 1 ? aspectRatio : 1;
		float h = aspectRatio < 1 ? aspectRatio : 1;

		if (perspective)
			GLA.glu.gluPerspective(45, aspectRatio, 1, 10000);
		else
			GLA.gl.glOrtho(-w, w, -h, h, 0, 1000);
	}
}