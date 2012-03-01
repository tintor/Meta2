package tintor.apps.knot_tying;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import tintor.apps.rigidbody.tools.GLA;

import com.sun.opengl.util.Animator;

public class Controller extends MouseAdapter implements GLEventListener, MouseMotionListener, KeyListener,
		MouseWheelListener {
	// Fields
	protected final View view = new View();
	protected int width, height; // of viewport

	private final Container container;
	private final Animator animator;

	private final GLCanvas canvas = new GLCanvas();

	// Parameters
	public boolean pause = true;
	public int steps = 4;

	// Constructor
	public Controller(final String title, final int width, final int height, final boolean fullscreen,
			final JApplet applet) {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		container = applet != null ? applet : new JFrame(title);

		container.setSize(width, height);

		canvas.setMinimumSize(new Dimension(100, 100));

		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		container.addKeyListener(this);

		animator = new Animator(canvas);
		if (container instanceof JFrame) {
			final JFrame frame = (JFrame) container;

			if (fullscreen) {
				GLA.switchToFullscreen(frame, width, height);
			}

			final Animator a = animator;
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(final WindowEvent e) {
					a.stop();
					frame.dispose();
				}
			});
		}
	}

	public void start() {
		animator.start();
		container.setVisible(true);
	}

	public void stop() {
		animator.stop();
	}

	public void close() {
		if (container instanceof JFrame) {
			stop();
			((JFrame) container).dispose();
		}
	}

	public void setTitle(final String title) {
		if (container instanceof JFrame) {
			((JFrame) container).setTitle(title);
		}
	}

	// GLEventListener
	@Override
	public void init(final GLAutoDrawable drawable) {
		drawable.addMouseListener(this);
		drawable.addMouseMotionListener(this);
		drawable.addMouseWheelListener(this);

		GLA.gl = drawable.getGL();

		System.out.println("OpenGL Vendor: " + GLA.gl.glGetString(GL.GL_VENDOR));
		System.out.println("OpenGL Version: " + GLA.gl.glGetString(GL.GL_VERSION));

		view.init();
	}

	@Override
	public void display(final GLAutoDrawable drawable) {
		GLA.gl = drawable.getGL();
		view.renderText = pause;
		view.display();
	}

	@Override
	public void displayChanged(final GLAutoDrawable drawable, final boolean arg1, final boolean arg2) {
	}

	@Override
	public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int w, final int h) {
		width = w;
		height = h;

		GLA.gl = drawable.getGL();
		view.reshape(x, y, w, h);
	}

	// KeyListener
	@Override
	public void keyPressed(final KeyEvent e) {
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}

	// MouseMotionListener
	@Override
	public void mouseDragged(final MouseEvent e) {
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
	}

	// MouseWheelListener
	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
	}

	public static void main(final String[] args) {
		new Controller("Simulator", 800, 600, false, null).start();
	}
}