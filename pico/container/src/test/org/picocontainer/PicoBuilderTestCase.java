/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer;

import com.thoughtworks.xstream.XStream;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.behaviors.ImplementationHiding;
import org.picocontainer.behaviors.Locking;
import org.picocontainer.behaviors.PropertyApplying;
import org.picocontainer.behaviors.Synchronizing;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.injectors.AnnotatedFieldInjection;
import org.picocontainer.injectors.AnnotatedMethodInjection;
import org.picocontainer.injectors.CompositeInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.NamedFieldInjection;
import org.picocontainer.injectors.NamedMethodInjection;
import org.picocontainer.injectors.SetterInjection;
import org.picocontainer.injectors.TypedFieldInjection;
import org.picocontainer.lifecycle.JavaEE5LifecycleStrategy;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.lifecycle.ReflectionLifecycleStrategy;
import org.picocontainer.lifecycle.StartableLifecycleStrategy;
import org.picocontainer.monitors.ConsoleComponentMonitor;
import org.picocontainer.monitors.NullComponentMonitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.picocontainer.behaviors.Behaviors.caching;
import static org.picocontainer.behaviors.Behaviors.implementationHiding;
import static org.picocontainer.behaviors.Behaviors.synchronizing;
import static org.picocontainer.injectors.Injectors.SDI;

@SuppressWarnings("serial")
public class PicoBuilderTestCase {

    private XStream xs;
    private EmptyPicoContainer parent = new EmptyPicoContainer();
    private NullLifecycleStrategy lifecycle = new NullLifecycleStrategy();
    private NullComponentMonitor ncm = new NullComponentMonitor();
    private AdaptingInjection ai = new AdaptingInjection();

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
        public CustomComponentFactory(SomeContainerDependency someDependency) {
        }

        public ComponentAdapter createComponentAdapter(ComponentMonitor monitor,
                                                       LifecycleStrategy lifecycle,
                                                       Properties componentProps,
                                                       Object key,
                                                       Class impl,
                                                       Parameter... parameters) throws PicoCompositionException {
            return null;
        }

        public void verify(PicoContainer container) {
        }

        public void accept(PicoVisitor visitor) {
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
        public TestPicoContainer(ComponentFactory componentFactory, ComponentMonitor monitor, LifecycleStrategy lifecycle, PicoContainer parent) {
            super(parent, lifecycle, monitor, componentFactory);
        }
    }

    private String toXml(Object expected) {
        return xs.toXML(expected);
    }


}
