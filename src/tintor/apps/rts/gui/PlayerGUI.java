package tintor.apps.rts.gui;

import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;

import tintor.apps.rts.core.OrderType;
import tintor.apps.rts.core.Server;
import tintor.apps.rts.core.Unit;
import tintor.apps.rts.core.Vector2;
import tintor.apps.rts.core.World;
import tintor.util.Host;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.j2d.TextRenderer;

public class PlayerGUI implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {
	final int myPlayer;
	final Host host;
	World world;
	int selectedUnit = -1;
	Frame frame;

	public static void create(final int myPlayer, final String server) {
		final PlayerGUI gui = new PlayerGUI(myPlayer, server);
		gui.frame = new Frame();
		final GLCanvas canvas = new GLCanvas();

		canvas.addGLEventListener(gui);
		gui.frame.add(canvas);
		canvas.setSize(900, 900);
		gui.frame.pack();
		gui.frame.setResizable(false);

		final Animator animator = new Animator(canvas);
		//animator.setRunAsFastAsPossible(true);
		gui.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				// Run this on another thread than the AWT event queue to
				// make sure the call to Animator.stop() completes before
				// exiting
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});
		gui.frame.addKeyListener(gui);
		gui.frame.setVisible(true);
		animator.start();
	}

	PlayerGUI(final int myPlayer, final String server) {
		this.myPlayer = myPlayer;
		host = Host.connect(server, Server.Port, false);
	}

	int circle;
	TextRenderer textRenderer;

	public void init(final GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		gl.setSwapInterval(1000);
		drawable.addMouseListener(this);
		drawable.addMouseMotionListener(this);
		drawable.addKeyListener(this);

		circle = gl.glGenLists(1);
		gl.glNewList(circle, GL.GL_COMPILE);
		gl.glBegin(GL.GL_POLYGON);
		final int k = 16;
		for (int i = 0; i < k; i++) {
			final double a = Math.PI * 2 * i / k;
			gl.glVertex3d(Math.cos(a), Math.sin(a), 0);
		}
		gl.glEnd();
		gl.glEndList();

		textRenderer = new TextRenderer(new Font("Courier New", Font.BOLD, 15));

		host.sendObject(myPlayer);
	}

	public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height) {
		final GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-450.0, 450.0, -450.0, 450.0, -10.0, 10.0);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	private static Vector2 screenToWorld(final int x, final int y) {
		return Vector2.create(x - 450.0f, 450.0f - y);
	}

	double xticks = 0;
	int ticks = 0;

	public void display(final GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glLoadIdentity();

		final World newWorld = (World) host.receiveObject();
		if (newWorld != null) {
			world = newWorld;
			frame.setTitle("Time: " + world.time + " " + (int) (xticks + 0.5));
			xticks = xticks * 0.95 + ticks * 0.05;
			ticks = 0;
		} else {
			ticks++;
		}

		if (world != null) {
			for (final Unit unit : world.units) {
				gl.glPushMatrix();
				gl.glTranslated(unit.position.x, unit.position.y, 0);
				if (unit.id == selectedUnit) {
					gl.glPushMatrix();
					gl.glColor3d(0.5, 1.0, 0.5);
					gl.glScaled(unit.radius + 4, unit.radius + 4, 1);
					gl.glCallList(circle);
					gl.glPopMatrix();
				}
				colorByPlayer(gl, unit.player);
				gl.glScaled(unit.radius, unit.radius, 1);
				gl.glCallList(circle);
				gl.glPopMatrix();
			}
		}

		gl.glPushMatrix();
		if (mouse != null) {
			gl.glTranslated(mouse.x, mouse.y, 0);
		}
		gl.glColor3d(1, 0, 0);
		gl.glScaled(3, 3, 1);
		gl.glCallList(circle);
		gl.glPopMatrix();

		textRenderer.beginRendering(drawable.getWidth(), drawable.getHeight());
		if (world != null && selectedUnit != -1) {
			final Unit unit = world.units.get(selectedUnit);
			int y = 100;
			final int dy = 13;
			textRenderer.draw(unit.type + " " + unit.id, 100, y -= dy);
			textRenderer.draw("player " + unit.player, 100, y -= dy);
			textRenderer.draw("HP " + unit.hitPoints + "/" + unit.maxHitPoints, 100, y -= dy);
			textRenderer.draw("minerals " + unit.minerals, 100, y -= dy);
		}
		if (world != null) {
			int y = 1000;
			final int dy = 13;
			for (int i = 1; i < world.minerals.length; i++) {
				if (world.minerals[i] > 0) {
					textRenderer.draw("player " + i + " : " + world.minerals[i] + " minerals", 10, y -= dy);
				}
			}
		}

		textRenderer.flush();
		textRenderer.endRendering();
	}

	void colorByPlayer(final GL gl, final int player) {
		switch (player) {
		case 0:
			gl.glColor3f(1, 1, 1);
			break;
		case 1:
			gl.glColor3f(0, 0, 1);
			break;
		case 2:
			gl.glColor3f(1, 0, 0);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void displayChanged(final GLAutoDrawable drawable, final boolean modeChanged, final boolean deviceChanged) {
	}

	public void mouseEntered(final MouseEvent e) {
	}

	public void mouseExited(final MouseEvent e) {
	}

	private Unit findByPosition(final Vector2 center, final double minRadius) {
		if (world == null)
			return null;

		for (final Unit unit : world.units) {
			final double radius = Math.max(minRadius, unit.radius);
			if (unit.position.distanceSquared(center) <= radius * radius)
				return unit;
		}
		return null;
	}

	Vector2 mouse;

	public void mousePressed(final MouseEvent e) {
		if (world == null)
			return;

		mouse = screenToWorld(e.getX(), e.getY());
		final Unit unit = findByPosition(mouse, 10);
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			selectedUnit = unit != null ? unit.id : -1;
			break;
		case MouseEvent.BUTTON3:
			if (selectedUnit != -1) {
				host.sendObject(unit != null ? OrderType.targetUnit(selectedUnit, unit.id) : OrderType.move(
						selectedUnit, mouse));
			}
			break;
		}
	}

	public void mouseReleased(final MouseEvent e) {
	}

	public void mouseClicked(final MouseEvent e) {
	}

	public void mouseDragged(final MouseEvent e) {
	}

	public void mouseMoved(final MouseEvent e) {
		mouse = screenToWorld(e.getX(), e.getY());
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case 'S':
			if (selectedUnit != -1) {
				host.sendObject(OrderType.stop(selectedUnit));
			}
			break;
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}
}
