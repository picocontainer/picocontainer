/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/
package org.picocontainer.injectors;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.tck.AbstractComponentFactoryTest;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.picocontainer.Characteristics.USE_NAMES;

/**
 * @author Mauro Talevi
 */
public class ConstructorInjectionTestCase extends AbstractComponentFactoryTest {

	@Before
    public void setUp() throws Exception {
        picoContainer = new DefaultPicoContainer(createComponentFactory());
    }

    protected ComponentFactory createComponentFactory() {
        return new ConstructorInjection();
    }

    public static class ClassA {
        private int x;
        public ClassA(int x) {
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
        private float x;
        public ClassB(float x) {
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
    @SuppressWarnings({"unchecked", "unused"})
    public static class ClassAsConstructor {
		private final Class type;

		public ClassAsConstructor(Class type) {
			this.type = type;    		
    	}
    }
    
	@Test 
    @SuppressWarnings("unchecked")
    public void allowClassTypesForComponentAdapter() {
        ConstructorInjection componentFactory = new ConstructorInjection();
        
        ConstructorInjector cica =  (ConstructorInjector)
        componentFactory.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), new Properties(), ClassAsConstructor.class, ClassAsConstructor.class, new ConstantParameter(String.class));
        
        ClassAsConstructor instance = (ClassAsConstructor) cica.getComponentInstance(picoContainer, ComponentAdapter.NOTHING.class);
        assertNotNull(instance);
    	
    }


}