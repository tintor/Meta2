package tintor.apps.rigidbody.model.solid;

import javax.media.opengl.GL;

import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Matrix3;
import tintor.geometry.Vector3;

final class Point extends Sphere {
	Point() {
		super(0.0f);
	}

	@Override
	public float mass() {
		return 1;
	}

	@Override
	public Matrix3 inertiaTensor() {
		return Matrix3.Identity;
	}

	@Override
	public void render() {
		// TODO restore prev point size
		GLA.gl.glPointSize(5);
		GLA.gl.glBegin(GL.GL_POINTS);
		GLA.vertex(Vector3.Zero);
		GLA.gl.glEnd();
	}
}