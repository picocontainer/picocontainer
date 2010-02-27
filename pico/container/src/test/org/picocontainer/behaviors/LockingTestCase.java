package org.picocontainer.behaviors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.picocontainer.Characteristics.LOCK;
import static org.picocontainer.Characteristics.NO_LOCK;

import org.junit.Test;
import org.picocontainer.ComponentFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.tck.AbstractComponentFactoryTest;


public class LockingTestCase extends AbstractComponentFactoryTest {

	private final ComponentFactory locking = new Locking().wrap(new AdaptingInjection());


	@Test
	public void testPicocontainerPropertiesIntegration() {
		MutablePicoContainer mpc = new PicoBuilder().withBehaviors(new Locking()).build();
		mpc.as(LOCK).addComponent("locked","It is locked");
		mpc.as(NO_LOCK).addComponent("not locked", "It is not locked");
		
		assertNotNull(mpc.getComponentAdapter("locked").findAdapterOfType(Locked.class));
		assertNull(mpc.getComponentAdapter("not locked").findAdapterOfType(Locked.class));
		
	}

	@Override
	protected ComponentFactory createComponentFactory() {
		return locking;
	}

}
