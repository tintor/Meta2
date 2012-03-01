package tintor.apps.rigidbody.controller;

import java.awt.BorderLayout;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.tools.GLA;
import tintor.apps.rigidbody.view.View;

import com.sun.opengl.util.Animator;

public class Controller extends MouseAdapter implements GLEventListener, MouseMotionListener, KeyListener,
		MouseWheelListener {
	// Fields
	protected World world;
	protected final View view = new View();
	protected int width, height; // of viewport

	private final Container container;
	private final JMenuBar menubar = new JMenuBar();
	private final Animator animator;

	private final GLCanvas canvas = new GLCanvas();
	SceneGraphModel model = new SceneGraphModel();
	protected final JTree sceneGraph = new JTree(model);
	protected final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, sceneGraph, canvas);

	// Parameters
	public boolean pause = true;
	public int steps = 4;

	// Constructor
	public Controller(final String title, final int width, final int height, final boolean fullscreen,
			final JApplet applet) {
		sceneGraph.setCellRenderer(new SceneGraphCellRenderer());

		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		container = applet != null ? applet : new JFrame(title);

		container.setSize(width, height);
		((RootPaneContainer) container).getRootPane().setJMenuBar(menubar);

		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(6);
		splitPane.setDividerLocation(150);
		container.add(splitPane, BorderLayout.CENTER);

		canvas.setMinimumSize(new Dimension(100, 100));

		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		container.addKeyListener(this);

		animator = new Animator(canvas);
		if (container instanceof JFrame) {
			final JFrame frame = (JFrame) container;

			if (fullscreen) GLA.switchToFullscreen(frame, width, height);

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

	protected JMenu menu;
	protected JMenuItem menuItem;

	protected JMenu menu(final String text) {
		menu = new JMenu(text);
		menubar.add(menu);
		return menu;
	}

	protected JMenuItem menuItem(final String text) {
		return menuItem(text, 0);
	}

	protected JMenuItem menuItem(final String text, final int key) {
		return menuItem(text, key, 0);
	}

	protected void menuSeparator() {
		menu.addSeparator();
	}

	protected JMenuItem menuItem(final String text, final int key, final int modifiers) {
		menuItem = new JMenuItem(text);
		if (key != 0) menuItem.setAccelerator(KeyStroke.getKeyStroke(key, modifiers));
		menu.add(menuItem);
		return menuItem;
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

	public void setWorld(final World world) {
		this.world = world;
		model.world = world;
		model.signalStructureChanged();
	}

	public void setTitle(final String title) {
		if (container instanceof JFrame) ((JFrame) container).setTitle(title);
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
		if (!pause) {
			world.step(steps);
			model.signalStructureChanged();
		}

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
}