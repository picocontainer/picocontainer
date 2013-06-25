package com.picocontainer.behaviors;

import static com.picocontainer.Characteristics.LOCK;
import static com.picocontainer.Characteristics.NO_LOCK;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import com.picocontainer.tck.AbstractComponentFactoryTest;

import com.picocontainer.ComponentFactory;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.behaviors.Locking;
import com.picocontainer.injectors.AdaptingInjection;


public class LockingTestCase extends AbstractComponentFactoryTest {

	private final ComponentFactory locking = new Locking().wrap(new AdaptingInjection());


	@Test
	public void testPicocontainerPropertiesIntegration() {
		MutablePicoContainer mpc = new PicoBuilder().withBehaviors(new Locking()).build();
		mpc.as(LOCK).addComponent("locked","It is locked");
		mpc.as(NO_LOCK).addComponent("not locked", "It is not locked");

		assertNotNull(mpc.getComponentAdapter("locked").findAdapterOfType(Locking.Locked.class));
		assertNull(mpc.getComponentAdapter("not locked").findAdapterOfType(Locking.Locked.class));

	}

	@Override
	protected ComponentFactory createComponentFactory() {
		return locking;
	}

}
