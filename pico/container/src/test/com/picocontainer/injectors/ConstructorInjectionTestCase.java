/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/
package com.picocontainer.injectors;

import static com.picocontainer.Characteristics.USE_NAMES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import com.picocontainer.tck.AbstractComponentFactoryTest;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.injectors.ConstructorInjection.ConstructorInjector;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.ConstantParameter;
import com.picocontainer.parameters.ConstructorParameters;

/**
 * @author Mauro Talevi
 */
public class ConstructorInjectionTestCase extends AbstractComponentFactoryTest {

	@Override
	@Before
    public void setUp() throws Exception {
        picoContainer = new DefaultPicoContainer(createComponentFactory());
    }

    @Override
	protected ComponentFactory createComponentFactory() {
        return new ConstructorInjection();
    }

    public static class ClassA {
        private final int x;
        public ClassA(final int x) {
            this.x = x;
        }
    }
    @Test public void testAutoConversionOfIntegerParam() {
        picoContainer.as(USE_NAMES).addComponent(ClassA.class);
        picoContainer.addComponent("x", "12");
        assertNotNull(picoContainer.getComponent(ClassA.class));
        assertEquals(12,picoContainer.getComponent(ClassA.class).x);
    }

    public static class ClassB {
        private final float x;
        public ClassB(final float x) {
            this.x = x;
        }
    }
    @Test public void testAutoConversionOfFloatParam() {
        picoContainer.as(USE_NAMES).addComponent(ClassB.class);
        picoContainer.addComponent("x", "1.2");
        assertNotNull(picoContainer.getComponent(ClassB.class));
        assertEquals(1.2,picoContainer.getComponent(ClassB.class).x, 0.0001);
    }


    /**
     * Test class to verify the CICA can handle
     * a constant parameter class type.
     *
     */
    @SuppressWarnings({"unused"})
    public static class ClassAsConstructor {
		private final Class<?> type;

		public ClassAsConstructor(final Class<?> type) {
			this.type = type;
    	}
    }

	@Test
    public void allowClassTypesForComponentAdapter() {
        ConstructorInjection componentFactory = new ConstructorInjection();

        ConstructorInjection.ConstructorInjector<ClassAsConstructor> cica =  (ConstructorInjection.ConstructorInjector<ClassAsConstructor>)
        componentFactory.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), new Properties(), ClassAsConstructor.class, ClassAsConstructor.class,
        		new ConstructorParameters(new ConstantParameter(String.class)),
        		null,
        		null);

        ClassAsConstructor instance = cica.getComponentInstance(picoContainer, ComponentAdapter.NOTHING.class);
        assertNotNull(instance);

    }

	@Test
	public void testOnlyParametersWithNullTargetNameAreUsed() {
        ConstructorInjection componentFactory = new ConstructorInjection();

        ConstructorInjection.ConstructorInjector<ClassAsConstructor> cica =  (ConstructorInjector<ClassAsConstructor>)
        componentFactory.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), new Properties(), ClassAsConstructor.class, ClassAsConstructor.class,
        		new ConstructorParameters(new ConstantParameter(String.class)),
        		null,
        		null);

        assertTrue(cica.parameters.length == 1);
        assertEquals(String.class, ((ConstantParameter)cica.parameters[0].getParams()[0]).getValue());
	}


}