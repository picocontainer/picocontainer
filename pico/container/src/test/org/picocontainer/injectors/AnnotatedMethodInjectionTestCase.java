/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.adapters.SimpleNamedBindingAnnotationTestCase.Apple;
import org.picocontainer.adapters.SimpleNamedBindingAnnotationTestCase.AppleImpl1;
import org.picocontainer.containers.JSRPicoContainer;
import org.picocontainer.containers.SomeQualifier;
import org.picocontainer.injectors.AnnotatedFieldInjection.AnnotatedFieldInjector;
import org.picocontainer.injectors.AnnotatedFieldInjectorTestCase.OrderChild;
import org.picocontainer.injectors.AnnotatedMethodInjection.AnnotatedMethodInjector;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.tck.AbstractComponentFactoryTest;
import org.picocontainer.tck.AbstractComponentAdapterTest.RecordingLifecycleStrategy;
import org.picocontainer.testmodel.NullLifecycle;
import org.picocontainer.testmodel.RecordingLifecycle.One;

/**
 * @author J&ouml;rg Schaible
 */
public class AnnotatedMethodInjectionTestCase extends AbstractComponentFactoryTest {
	
	@Before
    public void setUp() throws Exception {
        picoContainer = new DefaultPicoContainer(createComponentFactory());
    }

    protected ComponentFactory createComponentFactory() {
        return new AnnotatedMethodInjection();
    }

    public static interface Bean {
    }

    public static class NamedBean implements Bean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class NamedBeanWithPossibleDefault extends NamedBean {
        private boolean byDefault;

        public NamedBeanWithPossibleDefault() {
        }

        public NamedBeanWithPossibleDefault(String name) {
            setName(name);
            byDefault = true;
        }

        public boolean getByDefault() {
            return byDefault;
        }
    }

    public static class NoBean extends NamedBean {
        public NoBean(String name) {
            setName(name);
        }
    }

    @Test public void testContainerUsesStandardConstructor() {
        picoContainer.addComponent(Bean.class, NamedBeanWithPossibleDefault.class);
        picoContainer.addComponent("Tom");
        NamedBeanWithPossibleDefault bean = (NamedBeanWithPossibleDefault) picoContainer.getComponent(Bean.class);
        assertFalse(bean.getByDefault());
    }

    @Test public void testContainerUsesOnlyStandardConstructor() {
        picoContainer.addComponent(Bean.class, NoBean.class);
        picoContainer.addComponent("Tom");
        try {
            picoContainer.getComponent(Bean.class);
            fail("Instantiation should have failed.");
        } catch (PicoCompositionException e) {
        }
    }
    
    
    public static class DoSomething {
    	
    	public String a;
    	public String b;
    	public String c;

		@Inject
    	public void injectSomething(String a, @Named("b") String b, @SomeQualifier String c) {
			this.a = a;
			this.b = b;
			this.c = c;
    	}
    	
    }
    
    @Test
    public void testAnnationsOnParametersForMethodInjection() {
    	
    	MutablePicoContainer pico = new JSRPicoContainer(new DefaultPicoContainer());
    	
    	pico
    		.addComponent(String.class, "This is A test")
    		.addComponent("b", "This is B test")
    		.addComponent(SomeQualifier.class.getName(), "This is C test")
    		.addComponent(DoSomething.class);
    	
    	
    	DoSomething instance = pico.getComponent(DoSomething.class);
    	assertNotNull(instance);
    	
    	assertEquals("This is A test", instance.a);
    	assertEquals("This is B test", instance.b);
    	assertEquals("This is C test", instance.c);
    	
    }
    
    
    public static class OrderBase {
    	
    	protected static boolean oneInvoked = false;
    	
    	protected static boolean twoInvoked = false;
    	

    	@Inject
    	public static void one() {
    		assertFalse(oneInvoked);
    		assertFalse(twoInvoked);
    		
    		oneInvoked = true;
    	}

    	@Inject
    	public void two() {
    		assertTrue(oneInvoked);   		
    		assertFalse(twoInvoked);
    		
    		twoInvoked = true;
    	};
    }
    
    
    public static class OrderDerived extends OrderBase {
    	
    	protected static boolean threeInvoked = false;
    	
    	protected static boolean fourInvoked = false;
    	
    	@Inject
    	public static void three() {
    		assertTrue(oneInvoked);
    		assertTrue(twoInvoked);
    		assertFalse(threeInvoked);
    		assertFalse(fourInvoked);
    		threeInvoked = true;
    		
    	}

    	@Inject
    	public void four() {
    		assertTrue(oneInvoked);
    		assertTrue(twoInvoked);
    		assertTrue(threeInvoked);
    		assertFalse(fourInvoked);
    		fourInvoked = true;
    		
    	}
    	
    	public static void reset() {
    		oneInvoked = false;
    		twoInvoked = false;
    		threeInvoked = false;
    		fourInvoked = false;
    	}
    }
    
    
    @Test
    public void testBaseClassAndStaticsInjectedFirst() throws NoSuchMethodException {
    	OrderDerived.reset();

    	AnnotatedMethodInjector<OrderDerived> adapter = new AnnotatedMethodInjector<OrderDerived>(OrderDerived.class, OrderDerived.class, null, new NullComponentMonitor(), false, false, Inject.class);
    	OrderDerived derived = adapter.getComponentInstance(null, null);
    	assertTrue(OrderDerived.oneInvoked);
    	assertTrue(OrderDerived.twoInvoked);
    	assertTrue(OrderDerived.threeInvoked);
    	assertTrue(OrderDerived.fourInvoked);
    
    	OrderDerived.reset();
    }
    
    
    public static class StaticOneTime {
    	
    	public static Apple injectedApple;
    	
    	public Apple injectedApple2;
    	
    	@Inject
    	public static void injectApple(Apple a) {
    		injectedApple = a;
    	}
    	
    	@Inject
    	public void injectAnotherApple(Apple a) {
    		injectedApple2 = a;
    	}
    }
    
    @Test
    public void testStaticsAreOnlyInjectedOneTime() {
    	JSRPicoContainer pico = new JSRPicoContainer()
    			.addComponent(StaticOneTime.class)
    			.addComponent(Apple.class, AppleImpl1.class);
    	
    	StaticOneTime instance1 = pico.getComponent(StaticOneTime.class);
    	Apple static1 = StaticOneTime.injectedApple;
    	Apple nonStatic1 = instance1.injectedApple2;
    	
    	StaticOneTime instance2 = pico.getComponent(StaticOneTime.class);
    	
    	Apple static2 = StaticOneTime.injectedApple;
    	Apple nonStatic2 = instance2.injectedApple2;
    	
    	assertNotSame(instance1, instance2);
    	assertNotSame(nonStatic1, nonStatic2);
    	
    	//The important part :)
    	assertSame(static1, static2);
    }
    
}