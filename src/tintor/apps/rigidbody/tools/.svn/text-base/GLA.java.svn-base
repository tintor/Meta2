package tintor.apps.rigidbody.tools;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import tintor.geometry.Matrix3;
import tintor.geometry.Quaternion;
import tintor.geometry.Transform3;
import tintor.geometry.Vector3;

import com.sun.opengl.util.GLUT;

public final class GLA {
	public final static Vector3 blue = Vector3.create(0, 0, 1), white = Vector3.create(1, 1, 1), red = Vector3
			.create(1, 0, 0), green = Vector3.create(0, 1, 0), yellow = Vector3.create(1, 1, 0), orange = Vector3
			.create(1, 0.5, 0), gray = Vector3.create(0.5, 0.5, 0.5), black = Vector3.Zero, mangenta = Vector3
			.create(0, 1, 1), brown = Vector3.create(0.5, 0.25, 0);

	public static GL gl;
	public final static GLU glu = new GLU();
	public final static GLUT glut = new GLUT();

	public static void gluPickMatrix(final double x, final double y, final double deltaX, final double deltaY,
			final IntBuffer viewport) {
		if (deltaX <= 0 || deltaY <= 0) return;

		/* Translate and scale the picked region to the entire window */
		gl.glTranslated((viewport.get(2) - 2 * (x - viewport.get(0))) / deltaX,
				(viewport.get(3) - 2 * (y - viewport.get(1))) / deltaY, 0);
		gl.glScaled(viewport.get(2) / deltaX, viewport.get(3) / deltaY, 1.0);
	}

	public static void callList(final int list) {
		gl.glCallList(list);
	}

	public static void color(final Vector3 a) {
		gl.glColor3f(a.x, a.y, a.z);
	}

	public static void translate(final Vector3 a) {
		gl.glTranslated(a.x, a.y, a.z);
	}

	public static void rotate(final Quaternion q) {
		final Vector3 a = q.axis();
		gl.glRotated(q.angleDeg(), a.x, a.y, a.z);
	}

	public static void vertex(final float x, final float y, final float z) {
		gl.glVertex3f(x, y, z);
	}

	public static void vertex(final double x, final double y, final double z) {
		gl.glVertex3d(x, y, z);
	}

	public static void vertex(final Vector3 a) {
		gl.glVertex3f(a.x, a.y, a.z);
	}

	public static void normal(final float x, final float y, final float z) {
		gl.glNormal3f(x, y, z);
	}

	public static void normal(final double x, final double y, final double z) {
		gl.glNormal3d(x, y, z);
	}

	public static void normal(final Vector3 a) {
		gl.glNormal3f(a.x, a.y, a.z);
	}

	public static void multMatrix(final Transform3 transform) {
		final Matrix3 m = transform.rotation;
		final Vector3 v = transform.offset;
		final double[] a = new double[] { m.a.x, m.b.x, m.c.x, 0, m.a.y, m.b.y, m.c.y, 0, m.a.z, m.b.z, m.c.z, 0,
				v.x, v.y, v.z, 1 };
		gl.glMultMatrixd(a, a.length);
	}

	public static void multMatrix(final double[] m) {
		gl.glMultMatrixd(m, m.length);
	}

	public static void set(final int a, final boolean b) {
		if (b)
			gl.glEnable(a);
		else
			gl.glDisable(a);
	}

	public static void enable(final int a) {
		gl.glEnable(a);
	}

	public static void disable(final int a) {
		gl.glDisable(a);
	}

	public static void beginPoints() {
		gl.glBegin(GL.GL_POINTS);
	}

	public static void beginTriangleFan() {
		gl.glBegin(GL.GL_TRIANGLE_FAN);
	}

	public static void beginPolygon() {
		gl.glBegin(GL.GL_POLYGON);
	}

	public static void beginQuads() {
		gl.glBegin(GL.GL_QUADS);
	}

	public static void beginQuadStrip() {
		gl.glBegin(GL.GL_QUAD_STRIP);
	}

	public static void beginLineStrip() {
		gl.glBegin(GL.GL_LINE_STRIP);
	}

	public static void end() {
		gl.glEnd();
	}

	public static void loadTexture(final BufferedImage image, final int ix, final int iy, final int iw, final int ih) {
		GLA.gl.glTexImage2D(GL.GL_PROXY_TEXTURE_2D, 0, GL.GL_RGB, iw, ih, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, null);
		final int[] width = new int[] { -1 };
		GLA.gl.glGetTexParameteriv(GL.GL_PROXY_TEXTURE_2D, GL.GL_TEXTURE_WIDTH, width, 0);
		if (width[0] == 0) throw new RuntimeException("texture load failed!");

		final ByteBuffer z = ByteBuffer.allocateDirect(3 * iw * ih);
		for (int y = 0; y < ih; y++)
			for (int x = 0; x < iw; x++) {
				final int a = image.getRGB(ix + x, iy + y);
				z.put((byte) (a >> 16 & 0xFF));
				z.put((byte) (a >> 8 & 0xFF));
				z.put((byte) (a & 0xFF));
			}
		z.rewind();

		GLA.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, iw, ih, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, z);
	}

	public static void loadTextureRaw(final FileChannel channel, final int width, final int height, final int ix,
			final int iy, final int iw, final int ih) {
		GLA.gl.glTexImage2D(GL.GL_PROXY_TEXTURE_2D, 0, GL.GL_RGB, iw, ih, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, null);
		final int[] proxy_width = new int[] { -1 };
		GLA.gl.glGetTexParameteriv(GL.GL_PROXY_TEXTURE_2D, GL.GL_TEXTURE_WIDTH, proxy_width, 0);
		if (proxy_width[0] == 0) throw new RuntimeException("texture load failed!");

		final ByteBuffer z = ByteBuffer.allocate(3 * iw * ih);
		try {
			assert channel.size() == 3L * width * height;
			for (int y = 0; y < ih; y++) {
				final long p = ((long) iy + y) * width + ix;
				channel.position(p * 3);
				z.limit((y + 1) * iw * 3);
				final int r = channel.read(z);
				if (r != (long) iw * 3) throw new RuntimeException("underflow");
			}
			assert z.position() == z.limit();
			assert z.position() == z.capacity();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		z.rewind();

		GLA.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, iw, ih, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, z);
	}

	public static void switchToFullscreen(final Window window, final int width, final int height) {
		final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		device.setFullScreenWindow(window);

		final DisplayMode displayMode = catchDisplayMode(device.getDisplayModes(), width, height, device
				.getDisplayMode().getBitDepth(), device.getDisplayMode().getRefreshRate());

		device.setDisplayMode(displayMode);
	}

	private static final int DONT_CARE = -1;

	private static DisplayMode catchDisplayMode(final DisplayMode[] modes, final int width, final int height,
			final int depth, final int refreshRate) {
		DisplayMode mode = selectDisplayMode(modes, width, height, depth, refreshRate);
		if (mode != null) return mode;

		mode = selectDisplayMode(modes, width, height, depth, DONT_CARE);
		if (mode != null) return mode;

		mode = selectDisplayMode(modes, width, height, DONT_CARE, DONT_CARE);
		if (mode != null) return mode;

		mode = selectDisplayMode(modes, width, DONT_CARE, DONT_CARE, DONT_CARE);
		if (mode != null) return mode;

		return modes.length > 0 ? modes[0] : null;
	}

	private static DisplayMode selectDisplayMode(final DisplayMode[] modes, final int width, final int height,
			final int depth, final int refreshRate) {
		for (final DisplayMode mode : modes)
			if ((width == DONT_CARE || mode.getWidth() == width)
					&& (height == DONT_CARE || mode.getHeight() == height)
					&& (refreshRate == DONT_CARE || mode.getRefreshRate() == refreshRate)
					&& (depth == DONT_CARE || mode.getBitDepth() == depth)) return mode;
		return null;
	}
}