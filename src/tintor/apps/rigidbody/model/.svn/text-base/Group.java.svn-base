package tintor.apps.rigidbody.model;

import java.util.ArrayList;
import java.util.List;

import tintor.apps.rigidbody.model.joint.BallJoint;
import tintor.geometry.Vector3;

public class Group {
	private final String name;
	public final List<Object> list = new ArrayList<Object>();

	public Group(final String name) {
		this.name = name;
	}

	public void add(final Object obj) {
		list.add(obj);
	}

	public static Group hingeJoint(final Body a, final Body b, final Vector3 anchor, final float dz) {
		final Group g = new Group("HingeJoint");
		g.add(new BallJoint(a, b, anchor.add(dz, Vector3.Z)));
		g.add(new BallJoint(a, b, anchor.sub(dz, Vector3.Z)));
		return g;
	}

	@Override
	public String toString() {
		return name;
	}
}