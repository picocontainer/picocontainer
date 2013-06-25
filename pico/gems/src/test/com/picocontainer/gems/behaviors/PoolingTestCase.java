package com.picocontainer.gems.behaviors;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import com.picocontainer.gems.GemsCharacteristics;
import com.picocontainer.gems.PicoGemsBuilder;
import com.picocontainer.tck.AbstractComponentFactoryTest;

import com.picocontainer.ComponentFactory;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.injectors.AdaptingInjection;

public class PoolingTestCase extends AbstractComponentFactoryTest {

    private final ComponentFactory poolingComponentFactory = new Pooling().wrap(new AdaptingInjection());

    @Test
    public void testPicoIntegration() {
    	MutablePicoContainer mpc = new PicoBuilder().withBehaviors(PicoGemsBuilder.POOLING()).build();
    	mpc.as(GemsCharacteristics.NO_POOL).addComponent("NoPool","a")
    		.as(GemsCharacteristics.POOL).addComponent("Pooled", "b");

    	assertNull(mpc.getComponentAdapter("NoPool").findAdapterOfType(Pooling.Pooled.class));
    	assertNotNull(mpc.getComponentAdapter("Pooled").findAdapterOfType(Pooling.Pooled.class));
    }


	@Override
	protected ComponentFactory createComponentFactory() {
		return poolingComponentFactory;
	}

}
