package tintor.apps.rigidbody_classic.model.collisiondetection;

import tintor.apps.rigidbody_classic.model.Contact;

public interface ContactEvent {
	void contactEvent(Contact c);
}