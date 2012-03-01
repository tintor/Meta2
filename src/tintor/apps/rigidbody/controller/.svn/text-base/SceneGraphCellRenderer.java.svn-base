package tintor.apps.rigidbody.controller;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Effector;
import tintor.apps.rigidbody.model.Joint;
import tintor.apps.rigidbody.model.Sensor;

class SceneGraphCellRenderer extends DefaultTreeCellRenderer {
	ImageIcon bodyIcon = new ImageIcon("src/" + Controller.class.getPackage().getName().replace('.', '/')
			+ "/body.gif");

	ImageIcon jointIcon = new ImageIcon("src/" + Controller.class.getPackage().getName().replace('.', '/')
			+ "/joint.gif");

	ImageIcon effectorIcon = new ImageIcon("src/" + Controller.class.getPackage().getName().replace('.', '/')
			+ "/effector.gif");

	ImageIcon sensorIcon = new ImageIcon("src/" + Controller.class.getPackage().getName().replace('.', '/')
			+ "/sensor.gif");

	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
			final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (value instanceof Body)
			setIcon(bodyIcon);
		else if (value instanceof Joint)
			setIcon(jointIcon);
		else if (value instanceof Effector)
			setIcon(effectorIcon);
		else if (value instanceof Sensor) setIcon(sensorIcon);
		return this;
	}
}
