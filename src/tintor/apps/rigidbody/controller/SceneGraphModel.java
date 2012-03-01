package tintor.apps.rigidbody.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import tintor.apps.rigidbody.model.Group;
import tintor.apps.rigidbody.model.World;

class SceneGraphModel implements TreeModel {
	final Object root = "World";
	List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	List<Object> list = new ArrayList<Object>();
	World world;

	public void signalStructureChanged() {
		list.clear();
		world.populate(list);
		final TreeModelEvent e = new TreeModelEvent(this, new Object[] { root });
		for (final TreeModelListener l : listeners)
			l.treeStructureChanged(e);
	}

	public void signalNodesChanged(final Object node) {
		final TreeModelEvent e = new TreeModelEvent(this, new Object[] { node });
		for (final TreeModelListener l : listeners)
			l.treeNodesChanged(e);
	}

	@Override
	public int getChildCount(final Object parent) {
		if (parent == root) return list.size();
		final Group g = (Group) parent;
		return g.list.size();
	}

	@Override
	public Object getChild(final Object parent, final int index) {
		if (parent == root) return list.get(index);
		final Group g = (Group) parent;
		return g.list.get(index);
	}

	@Override
	public int getIndexOfChild(final Object parent, final Object child) {
		final int count = getChildCount(parent);
		for (int i = 0; i < count; i++)
			if (getChild(parent, i).equals(child)) return i;
		throw new RuntimeException();
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(final Object node) {
		return node != root && !(node instanceof Group);
	}

	@Override
	public void valueForPathChanged(final TreePath path, final Object newValue) {
	}

	@Override
	public void addTreeModelListener(final TreeModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTreeModelListener(final TreeModelListener l) {
		listeners.remove(l);
	}

}

//class Node {
//	String name;
//	Node[] children;
//
//	Node(final String name, final Node... children) {
//		this.name = name;
//		this.children = children;
//	}
//
//	@Override
//	public String toString() {
//		return name;
//	}
//}