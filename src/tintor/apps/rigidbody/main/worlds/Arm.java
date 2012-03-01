package tintor.apps.rigidbody.main.worlds;

import java.awt.event.KeyEvent;

import tintor.apps.rigidbody.model.Body;
import tintor.apps.rigidbody.model.Group;
import tintor.apps.rigidbody.model.World;
import tintor.apps.rigidbody.model.effector.Drag;
import tintor.apps.rigidbody.model.effector.MuscleServo;
import tintor.apps.rigidbody.model.solid.Solid;
import tintor.apps.rigidbody.tools.GLA;
import tintor.geometry.Quaternion;
import tintor.geometry.Vector3;

public class Arm extends World {
	MuscleServo shoulderServo, elbowServo, handServo;

	public Arm() {
		final Group arm = new Group("Arm");

		final Body base = new Body(Vector3.create(0, -6.5, 0), Quaternion.Identity, Solid.box(2.5f, 1.5f, 2.5f)
				.density(5).color(GLA.orange).compile());
		base.name = "Base";
		arm.add(base);

		final Body lowArm = new Body(Vector3.create(5.5, -4, 0), Quaternion.Identity, Solid.box(5f, 0.5f, 0.5f)
				.color(GLA.orange).compile());
		lowArm.name = "Low arm";
		arm.add(lowArm);

		arm.add(Group.hingeJoint(base, lowArm, Vector3.create(0, -4, 0), 1));
		shoulderServo = new MuscleServo(base, lowArm, Vector3.create(2.5, -6.5, 0), Vector3.create(5.5, -5, 0));
		shoulderServo.active = true;
		shoulderServo.maxForce = 200;
		arm.add(shoulderServo);

		final Body highArm = new Body(Vector3.create(16.5, -4, 0), Quaternion.Identity, Solid.box(5, 0.5f, 0.5f)
				.color(GLA.orange).compile());
		highArm.name = "High arm";
		arm.add(highArm);

		arm.add(Group.hingeJoint(lowArm, highArm, Vector3.create(11, -4, 0), 1));
		elbowServo = new MuscleServo(lowArm, highArm, Vector3.create(5.5, -5, 0), Vector3.create(16.5, -5, 0));
		elbowServo.active = true;
		elbowServo.maxForce = 200;
		arm.add(elbowServo);

		final Body hand = new Body(Vector3.create(24, -4, 0), Quaternion.Identity, Solid.box(1.5f, 0.125f, 1.5f)
				.color(GLA.red).compile());
		hand.name = "Hand";
		arm.add(hand);

		arm.add(Group.hingeJoint(highArm, hand, Vector3.create(22, -4, 0), 1));
		handServo = new MuscleServo(highArm, hand, Vector3.create(16.5, -5, 0), Vector3.create(24, -5, 0));
		handServo.active = true;
		handServo.maxForce = 200;
		arm.add(handServo);

		add(arm);

		final Body box = new Body(Vector3.create(16, -7, 0), Quaternion.Identity, Solid.cube(1).density(0.5f)
				.color(GLA.white).compile());
		box.name = "Box";
		add(box);

		add(new Drag());
		surface(-8, 1);
	}

	@Override
	public void keyDown(final int key) {
		switch (key) {
		case KeyEvent.VK_Z:
			handServo.goalPos += 0.25;
			break;
		case KeyEvent.VK_X:
			handServo.goalPos -= 0.25;
			break;
		case KeyEvent.VK_LEFT:
			elbowServo.goalPos -= 0.25;
			break;
		case KeyEvent.VK_RIGHT:
			elbowServo.goalPos += 0.25;
			break;
		case KeyEvent.VK_UP:
			shoulderServo.goalPos += 0.25;
			break;
		case KeyEvent.VK_DOWN:
			shoulderServo.goalPos -= 0.25;
			break;
		case KeyEvent.VK_C:
			shoulderServo.active = !shoulderServo.active;
			elbowServo.active = !elbowServo.active;
			handServo.active = !handServo.active;
			break;
		}
	}
}