package org.picocontainer.modules;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

public class PublisherTestCase {
	
	private MutablePicoContainer parent;
	
	private MutablePicoContainer child;
	
	private Publisher publisher;

	public static class A {
		
	}

	@Before
	public void setUp() throws Exception {
		parent = new PicoBuilder().withCaching().build();
		child = parent.makeChildContainer();
		child.addComponent(A.class);
		publisher = new Publisher(child, parent);
	}
	

	@Test
	public void testPublishWithNullKeyGetsAppropriateNPEEarlyOn() {
		try {
			publisher.publish(null);
		} catch (NullPointerException e) {
			assertEquals("key", e.getMessage());
		}
	}
	
	@Test
	public void testPromotion() {
		assertNull(parent.getComponent(A.class));
		assertNotNull(child.getComponent(A.class));
		publisher.publish(A.class);
		assertNotNull(parent.getComponent(A.class));
		assertNotNull(child.getComponent(A.class));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMissingClassInChildThrowsIllegalArgumentExceptions() {
		publisher.publish("B");
	}
	
	

}
