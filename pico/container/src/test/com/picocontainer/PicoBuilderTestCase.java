/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer;

import static com.picocontainer.behaviors.Behaviors.caching;
import static com.picocontainer.behaviors.Behaviors.implementationHiding;
import static com.picocontainer.behaviors.Behaviors.synchronizing;
import static com.picocontainer.injectors.Injectors.SDI;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.LifecycleStrategy;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoVisitor;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.ImplementationHiding;
import com.picocontainer.behaviors.Locking;
import com.picocontainer.behaviors.PropertyApplying;
import com.picocontainer.behaviors.Synchronizing;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.AdaptingInjection;
import com.picocontainer.injectors.AnnotatedFieldInjection;
import com.picocontainer.injectors.AnnotatedMethodInjection;
import com.picocontainer.injectors.CompositeInjection;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.injectors.NamedFieldInjection;
import com.picocontainer.injectors.NamedMethodInjection;
import com.picocontainer.injectors.SetterInjection;
import com.picocontainer.injectors.TypedFieldInjection;
import com.picocontainer.lifecycle.JavaEE5LifecycleStrategy;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import com.picocontainer.lifecycle.StartableLifecycleStrategy;
import com.picocontainer.monitors.ConsoleComponentMonitor;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.FieldParameters;
import com.picocontainer.parameters.MethodParameters;
import com.thoughtworks.xstream.XStream;

@SuppressWarnings("serial")
public class PicoBuilderTestCase {

    private XStream xs;
    private final EmptyPicoContainer parent = new EmptyPicoContainer();
    private final NullLifecycleStrategy lifecycle = new NullLifecycleStrategy();
    private final NullComponentMonitor ncm = new NullComponentMonitor();
    private final AdaptingInjection ai = new AdaptingInjection();

    @Before
    public void setUp() throws Exception {
        xs = new XStream();
        xs.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    }

    @Test public void testDefaultHasNullComponentManagerAndNullLifecycleAndAdaptingInjection() {
        Object actual = new PicoBuilder().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithStartableLifecycle() {
        Object actual = new PicoBuilder().withLifecycle().build();
        Object expected = new DefaultPicoContainer(parent, new StartableLifecycleStrategy(ncm), ncm, ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    public static class FooLifecycleStrategy extends NullLifecycleStrategy{
    }

    @Test public void testWithCustomLifecycle() {
        Object actual = new PicoBuilder().withLifecycle(FooLifecycleStrategy.class).build();
        Object expected = new DefaultPicoContainer(parent, new FooLifecycleStrategy(), ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithCustomLifecycle2() {
        Object actual = new PicoBuilder().withLifecycle(new FooLifecycleStrategy()).build();
        Object expected = new DefaultPicoContainer(parent, new FooLifecycleStrategy(), ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithJEE5Lifecycle() {

        Object actual = new PicoBuilder().withJavaEE5Lifecycle().build();
        Object expected = new DefaultPicoContainer(parent, new JavaEE5LifecycleStrategy(ncm), ncm, ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithLifecycleInstance() {

        Object actual = new PicoBuilder().withLifecycle(new FooLifecycleStrategy()).build();
        Object expected = new DefaultPicoContainer(parent, new FooLifecycleStrategy(), ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testThatLastOfInstanceOrClassLifecycleIsDominant() {
        Object actual = new PicoBuilder().withLifecycle(new FooLifecycleStrategy()).withLifecycle().build();
        Object expected = new DefaultPicoContainer(parent, new StartableLifecycleStrategy(ncm), ncm, ai);
        assertEquals(toXml(expected), toXml(actual));
        actual = new PicoBuilder().withLifecycle().withLifecycle(new FooLifecycleStrategy()).build();
        expected = new DefaultPicoContainer(parent, new FooLifecycleStrategy(), ai
       );
        assertEquals(toXml(expected), toXml(actual));
    }


    @Test public void testWithReflectionLifecycle() {
        Object actual = new PicoBuilder().withReflectionLifecycle().build();
        Object expected = new DefaultPicoContainer(parent, new ReflectionLifecycleStrategy(ncm), ncm, ai);
        assertEquals(toXml(expected), toXml(actual));
    }


    @Test public void testWithConsoleMonitor() {
        Object actual = new PicoBuilder().withConsoleMonitor().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, new ConsoleComponentMonitor(), ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithConsoleMonitorAndLifecycleUseTheSameUltimateMonitor() {
        Object actual = new PicoBuilder().withLifecycle().withConsoleMonitor().build();
        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        Object expected = new DefaultPicoContainer(parent, new StartableLifecycleStrategy(cm), cm, ai);
        assertEquals(toXml(expected), toXml(actual));
    }


    @Test public void testWithCustomMonitorByClass() {
        Object actual = new PicoBuilder().withMonitor(ConsoleComponentMonitor.class).build();
        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        Object expected = new DefaultPicoContainer(parent, lifecycle, cm, ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    @SuppressWarnings({ "unchecked" })
    @Test public void testWithBogusCustomMonitorByClass() {
        // We do unchecked assignment so we test what its really doing, and smart IDE's don't complain
        try {
            Class aClass = HashMap.class;
            new PicoBuilder().withMonitor(aClass).build();
            fail("should have barfed");
        } catch (ClassCastException e) {
            // expected
        }
    }

    @Test public void testWithImplementationHiding() {
        Object actual = new PicoBuilder().withHiddenImplementations().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new ImplementationHiding().wrap(ai));
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithImplementationHidingInstance() {
        Object actual = new PicoBuilder().withComponentFactory(new ImplementationHiding()).build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new ImplementationHiding().wrap(ai));
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithInjectionFactoryChain() {
        Object actual = new PicoBuilder(SDI()).withBehaviors(caching(), synchronizing(), implementationHiding()).build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new Caching().wrap(new Synchronizing()
                .wrap(new ImplementationHiding().wrap(new SetterInjection()))));
        assertEquals(toXml(expected), toXml(actual));
    }

    public static class CustomParentcontainer extends EmptyPicoContainer {}

    @Test public void testCustomParentContainer() {
        Object actual = new PicoBuilder(new CustomParentcontainer()).build();
        Object expected = new DefaultPicoContainer(new CustomParentcontainer(), lifecycle, ncm, ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testBogusParentContainerBehavesAsIfNotSet() {
        Object actual = new PicoBuilder((PicoContainer)null).build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, ai);
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testParentAndChildContainersMutallyVisible() {
        MutablePicoContainer parent = new PicoBuilder().build();
        MutablePicoContainer actual = new PicoBuilder(parent).addChildToParent().build();

        MutablePicoContainer parentExpected = new PicoBuilder().build();
        MutablePicoContainer expected = new DefaultPicoContainer(parentExpected, lifecycle, ncm, ai);
        parentExpected.addChildContainer(expected);

        assertEquals(toXml(expected), toXml(actual));
        boolean b = parent.removeChildContainer(actual);
        assertTrue(b);
    }

    @Test
    public void testParentAndChildContainersVetoedWhenParentNotMutable() {
        try {
            new PicoBuilder(parent).addChildToParent().build();
            fail("should have barfed");
        } catch (PicoCompositionException e) {
            assertTrue(e.getMessage().contains("parent must be a MutablePicoContainer"));
        }
    }

    @Test public void testWithSetterInjection() {
        Object actual = new PicoBuilder().withSetterInjection().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new SetterInjection());
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithConstructorAndSetterInjectionMakesHiddenCompositeInjection() {
        Object actual = new PicoBuilder().withConstructorInjection().withSetterInjection().build();
        Object expected = new DefaultPicoContainer(
                parent, lifecycle, ncm, new CompositeInjection(new ConstructorInjection(), new SetterInjection()));
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithAnnotatedMethodDI() {
        Object actual = new PicoBuilder().withAnnotatedMethodInjection().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new AnnotatedMethodInjection());
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithAnnotatedFieldDI() {
        Object actual = new PicoBuilder().withAnnotatedFieldInjection().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new AnnotatedFieldInjection());
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithTypedFieldDI() {
        Object actual = new PicoBuilder().withTypedFieldInjection().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new TypedFieldInjection());
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithCtorDI() {
        Object actual = new PicoBuilder().withConstructorInjection().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new ConstructorInjection());
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithNamedMethodInjection() {
        Object actual = new PicoBuilder().withNamedMethodInjection().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new NamedMethodInjection());
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithNamedFieldInjection() {
        Object actual = new PicoBuilder().withNamedFieldInjection().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new NamedFieldInjection());
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithImplementationHidingAndSetterDI() {
        Object actual = new PicoBuilder().withHiddenImplementations().withSetterInjection().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new ImplementationHiding().wrap(new SetterInjection())
       );
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithCachingImplementationHidingAndSetterDI() {
        Object actual = new PicoBuilder().withCaching().withHiddenImplementations().withSetterInjection().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new Caching().wrap(new ImplementationHiding().wrap(new SetterInjection()))
       );
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithSynchronizing() {
        Object actual = new PicoBuilder().withSynchronizing().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new Synchronizing().wrap(ai));
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithLocking() {
        Object actual = new PicoBuilder().withLocking().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new Locking().wrap(ai));
        assertEquals(toXml(expected), toXml(actual));
    }

    @Test public void testWithPropertyApplier() {
        Object actual = new PicoBuilder().withPropertyApplier().build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new PropertyApplying().wrap(ai));
        assertEquals(toXml(expected), toXml(actual));
    }

    //TODO - fix up to refer to SomeContainerDependency
    @Test public void testWithCustomComponentFactory() {
        Object actual = new PicoBuilder().withCustomContainerComponent(new SomeContainerDependency()).withComponentFactory(CustomComponentFactory.class).build();
        Object expected = new DefaultPicoContainer(parent, lifecycle, ncm, new CustomComponentFactory(new SomeContainerDependency())
       );
        assertEquals(toXml(expected), toXml(actual));
    }

    public static class SomeContainerDependency {
    }
    public static class CustomComponentFactory implements ComponentFactory {

        @SuppressWarnings({ "UnusedDeclaration" })
        public CustomComponentFactory(final SomeContainerDependency someDependency) {
        }

        public ComponentAdapter createComponentAdapter(final ComponentMonitor monitor,
                                                       final LifecycleStrategy lifecycle,
                                                       final Properties componentProps,
                                                       final Object key,
                                                       final Class impl,
                                                       final ConstructorParameters constructorParams, final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {
            return null;
        }

		public void dispose() {
			
		}
        
        public void verify(final PicoContainer container) {
        }

        public void accept(final PicoVisitor visitor) {
            visitor.visitComponentFactory(this);
        }
    }


    @Test public void testWithCustomPicoContainer() {
        Object actual = new PicoBuilder().implementedBy(TestPicoContainer.class).build();
        Object expected = new TestPicoContainer(ai, ncm, lifecycle, parent);
        assertEquals(toXml(expected), toXml(actual));
    }


    @Test
    public void testMultipleUsesAreSupported() {
        PicoBuilder picoBuilder = new PicoBuilder().withCaching().withLifecycle();
        MutablePicoContainer pico = picoBuilder.build();

        pico.addComponent(Map.class, HashMap.class);
        assertNotNull(pico.getComponentAdapter(Map.class).findAdapterOfType(Caching.Cached.class));

        pico = picoBuilder.build();
        pico.addComponent(Map.class, HashMap.class);
        assertNotNull(pico.getComponentAdapter(Map.class).findAdapterOfType(Caching.Cached.class));


    }


    public static class TestPicoContainer extends DefaultPicoContainer {
        public TestPicoContainer(final ComponentFactory componentFactory, final ComponentMonitor monitor, final LifecycleStrategy lifecycle, final PicoContainer parent) {
            super(parent, lifecycle, monitor, componentFactory);
        }
    }

    private String toXml(final Object expected) {
        return xs.toXML(expected);
    }


}
