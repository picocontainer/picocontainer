/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

import static com.picocontainer.injectors.NamedFieldInjection.injectionFieldNames;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.Test;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.ComponentMonitorStrategy;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.ComponentAdapter.NOTHING;
import com.picocontainer.annotations.Inject;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.AdaptingInjection;
import com.picocontainer.injectors.AnnotatedMethodInjection;
import com.picocontainer.injectors.CompositeInjection;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.injectors.NamedFieldInjection;
import com.picocontainer.injectors.SetterInjection;
import com.picocontainer.injectors.CompositeInjection.CompositeInjector;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.WriterComponentMonitor;

/**
 * @author Paul Hammant
 */
public class CompositeInjectionTestCase {

    public static class Bar {
    }
    public static class Baz {
    }

    public static class Foo {
        private final Bar bar;
        private Baz baz;

        public Foo(final Bar bar) {
            this.bar = bar;
        }

        public void setBaz(final Baz baz) {
            this.baz = baz;
        }
    }

    public static class Foo2 {
        private final Bar bar;
        private Baz baz;

        public Foo2(final Bar bar) {
            this.bar = bar;
        }

        public void injectBaz(final Baz baz) {
            this.baz = baz;
        }
    }

    public static class Foo3 {
        private final Bar bar;
        private Baz baz;

        public Foo3(final Bar bar) {
            this.bar = bar;
        }

        @Inject
        public void fjshdfkjhsdkfjh(final Baz baz) {
            this.baz = baz;
        }
    }
    public static class Foo4 {
        private final Bar bar;
        private String one;
        private String two;

        public Foo4(final Bar bar) {
            this.bar = bar;
        }

    }

    @Test public void testComponentWithCtorAndSetterDiCanHaveAllDepsSatisfied() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(), new NullLifecycleStrategy(), new CompositeInjection(new ConstructorInjection(), new SetterInjection()));
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo.class);
        Foo foo = dpc.getComponent(Foo.class);
        assertNotNull(foo);
        assertNotNull(foo.bar);
        assertNotNull(foo.baz);
    }

    @Test public void testComponentWithCtorAndSetterDiCanHaveAllDepsSatisfiedWithANonSetInjectMethod() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(), new NullLifecycleStrategy(), new CompositeInjection(new ConstructorInjection(), new SetterInjection("inject")));
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo2.class);
        Foo2 foo = dpc.getComponent(Foo2.class);
        assertNotNull(foo);
        assertNotNull(foo.bar);
        assertNotNull(foo.baz);
    }

    @Test public void testComponentWithCtorAndMethodAnnotatedDiCanHaveAllDepsSatisfied() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(), new NullLifecycleStrategy(), new CompositeInjection(new ConstructorInjection(), new AnnotatedMethodInjection()));
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo3.class);
        Foo3 foo = dpc.getComponent(Foo3.class);
        assertNotNull(foo);
        assertNotNull(foo.bar);
        assertNotNull(foo.baz);
    }


    @Test public void testComponentWithCtorAndNamedFieldWorkToegether() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(), new NullLifecycleStrategy(), new CompositeInjection(new ConstructorInjection(), new NamedFieldInjection()));
        dpc.addComponent(Bar.class);
        dpc.addConfig("one", "1");
        dpc.addConfig("two", "2");
        dpc.as(injectionFieldNames("one", "two")).addComponent(Foo4.class);
        Foo4 foo4 = dpc.getComponent(Foo4.class);
        assertNotNull(foo4);
        assertNotNull(foo4.bar);
        assertNotNull(foo4.one);
        assertEquals("1", foo4.one);
        assertNotNull(foo4.two);
        assertEquals("2", foo4.two);
    }

    @Test public void testWithNonNullLifecycleStrategy() {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(),
                new NonNullLifecycleStrategy(),
                new CompositeInjection(new ConstructorInjection(), new AnnotatedMethodInjection())
       );
        dpc.addComponent(Bar.class);
        assertNotNull(dpc.getComponent(Bar.class));
    }

    @Test
    public void testChangeMonitor() {
        DefaultPicoContainer dpc = new DefaultPicoContainer();

		StringWriter writer1 = new StringWriter();
		ComponentMonitor monitor1 = new WriterComponentMonitor(writer1);


		StringWriter writer2 = new StringWriter();
		ComponentMonitor monitor2 = new WriterComponentMonitor(writer2) {

		};

		dpc.changeMonitor(monitor1);
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo.class);
        dpc.changeMonitor(monitor2);

        ComponentAdapter<?> adapter = dpc.getComponentAdapter(Foo.class);
    	ComponentAdapter<?> current = adapter;
        while(current != null) {
        	if (current instanceof ComponentMonitorStrategy) {
        		ComponentMonitorStrategy castValue = (ComponentMonitorStrategy)current;
	        	assertTrue("Failed on " + current.getDescriptor(),  castValue.currentMonitor() == monitor2);
        	}
        	current = current.getDelegate();
        }

    }

    private static class NonNullLifecycleStrategy implements LifecycleStrategy {
        public void start(final Object component) {
        }

        public void stop(final Object component) {
        }

        public void dispose(final Object component) {
        }

        public boolean hasLifecycle(final Class<?> type) {
            return false;
        }

        public boolean isLazy(final ComponentAdapter<?> adapter) {
            return false;
        }
    }

    public static class CompositeOrderBase {

    	@Inject
    	public static String one = null;

    	protected boolean injectSomethingCalled = false;

    	protected static boolean staticInjectSomethingCalled = false;

    	@Inject
    	public static void injectSomethingStatic() {
    		staticInjectSomethingCalled = true;
    	}

    	@Inject
    	public void injectSomething() {
    		injectSomethingCalled = true;
    		//assertNotNull(one);  -- Ignored since its static
    		assertNull(CompositeOrderDerived.two);
    	}

    }

    public static class CompositeOrderDerived extends CompositeOrderBase {

    	@Inject
    	public static String two = null;

    	public boolean injectSomethingElseCalled = false;

    	@Inject
    	public static void injectSomethingElseStatic() {
    		assertTrue("Base class static needs to be called before subtype static", CompositeOrderBase.staticInjectSomethingCalled);
    	}

    	@Inject
    	public void injectSomethingElse() {
    		injectSomethingElseCalled = true;
    		//assertNotNull(CompositeOrderBase.one);
    		assertTrue(injectSomethingCalled);
    		//assertNotNull(two);
    	}
    }


    @Test
    public void testParentFieldsAndMethodsAreInjectedBeforeSubtypeFieldsAndMethods() {
    	CompositeOrderBase.one = null;
    	CompositeOrderDerived.two = null;

    	DefaultPicoContainer container = new DefaultPicoContainer(new AdaptingInjection());

    	container.addComponent(CompositeOrderDerived.class)
    			.addComponent(String.class, "Testing");

    	CompositeOrderDerived derived = container.getComponent(CompositeOrderDerived.class);
    	assertNotNull(derived);
    	checkFields(derived);
    }


	private void checkFields(final CompositeOrderDerived derived) {
		assertTrue(derived.injectSomethingCalled);
    	assertTrue(derived.injectSomethingElseCalled);
    	//Skipped because of statics
    	//assertEquals("Testing", CompositeOrderBase.one);
    	//assertEquals("Testing", CompositeOrderDerived.two);
	}

	@Test
    @SuppressWarnings("rawtypes")
    public void testDecorationOfInstantatedInstanceAlsoReceivesProperInstantiationOrdering() {
    	CompositeOrderBase.one = null;
    	CompositeOrderDerived.two = null;

    	DefaultPicoContainer container = new DefaultPicoContainer(new AdaptingInjection());

    	container.addComponent(CompositeOrderDerived.class)
    			.addComponent(String.class, "Testing");

    	CompositeInjector adapter = container.getComponentAdapter(CompositeOrderDerived.class).findAdapterOfType(CompositeInjector.class);
    	assertNotNull(adapter);

    	CompositeOrderDerived instance = new CompositeOrderDerived();
    	adapter.decorateComponentInstance(container, NOTHING.class, instance);
    	checkFields(instance);
    }

}