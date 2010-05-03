/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.behaviors;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.tck.AbstractComponentFactoryTest;


public class ImplementationHidingTestCase extends AbstractComponentFactoryTest{

    @Test public void testAddComponentUsesImplementationHidingBehavior() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new ImplementationHiding().wrap(new ConstructorInjection()));
        pico.addComponent("foo", String.class);
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(ImplementationHiding.HiddenImplementation.class, foo.getClass());
        assertEquals(ConstructorInjection.ConstructorInjector.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());
    }

    @Test public void testAddComponentUsesImplementationHidingBehaviorWithRedundantHideImplProperty() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new ImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.HIDE_IMPL).addComponent("foo", String.class);
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(ImplementationHiding.HiddenImplementation.class, foo.getClass());
        assertEquals(ConstructorInjection.ConstructorInjector.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());
    }

    @Test public void testAddComponentNoesNotUseImplementationHidingBehaviorWhenNoCachePropertyIsSpecified() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new ImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.NO_HIDE_IMPL).addComponent("foo", String.class);
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(ConstructorInjection.ConstructorInjector.class, foo.getClass());
    }

    @Test public void testAddAdapterUsesImplementationHidingBehavior() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new ImplementationHiding().wrap(new ConstructorInjection()));
        pico.addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(ImplementationHiding.HiddenImplementation.class, foo.getClass());
        assertEquals(InstanceAdapter.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());
    }

    @Test public void testAddAdapterUsesImplementationHidingBehaviorWithRedundantHideImplProperty() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new ImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.HIDE_IMPL).addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(ImplementationHiding.HiddenImplementation.class, foo.getClass());
        assertEquals(InstanceAdapter.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());
    }

    @Test public void testAddAdapterNoesNotUseImplementationHidingBehaviorWhenNoCachePropertyIsSpecified() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new ImplementationHiding().wrap(new ConstructorInjection()));
        pico.change(Characteristics.NO_HIDE_IMPL).addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter<?> foo = pico.getComponentAdapter("foo");
        assertEquals(InstanceAdapter.class, foo.getClass());
    }


    private final ComponentFactory implementationHidingComponentFactory =
        new ImplementationHiding().wrap(new AdaptingInjection());

    protected ComponentFactory createComponentFactory() {
        return implementationHidingComponentFactory;
    }


    public static interface NeedsStringBuilder {
        void foo();
    }
    public static class NeedsStringBuilderImpl implements NeedsStringBuilder {
        StringBuilder sb;

        public NeedsStringBuilderImpl(StringBuilder sb) {
            this.sb = sb;
            sb.append("<init>");
        }
        public void foo() {
            sb.append("foo()");
        }
    }
    public static class NeedsNeedsStringBuilder {

        NeedsStringBuilder nsb;

        public NeedsNeedsStringBuilder(NeedsStringBuilder nsb) {
            this.nsb = nsb;
        }
        public void foo() {
            nsb.foo();
        }
    }

    @Test public void testLazyInstantiationSideEffectWhenForceOfDelayedInstantiationOfDependantClass() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new ImplementationHiding().wrap(new Caching().wrap(new ConstructorInjection())));
        pico.addComponent(StringBuilder.class);
        pico.addComponent(NeedsStringBuilder.class, NeedsStringBuilderImpl.class);
        pico.addComponent(NeedsNeedsStringBuilder.class);
        NeedsNeedsStringBuilder nnsb = pico.getComponent(NeedsNeedsStringBuilder.class);
        assertNotNull(nnsb);
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals("", sb.toString()); // not instantiated yet
        nnsb.foo();
        assertEquals("<init>foo()", sb.toString()); // instantiated
    }

    //@Test public void shouldNotInstantiateForEveryMethodCall() {
    // ...
    //}

    @Test public void shouldInstantiateForEveryMethodCall() {
        cachingTestBody("<init>foo()<init>foo()", new ImplementationHiding());
    }

    @Test public void shouldNotInstantiateForEveryMethodCallIfCaching() {
        cachingTestBody("<init>foo()foo()", new ImplementationHiding().wrap(new Caching()));
    }

    @Test public void shouldInstantiateForEveryMethodCallIfCachingWrapsImplementationHidingWhichIsFutile() {
        cachingTestBody("<init>foo()<init>foo()", new Caching().wrap(new ImplementationHiding()));
    }

    private void cachingTestBody(String expectation, ComponentFactory compFactory) {
        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        parent.addComponent(StringBuilder.class);
        DefaultPicoContainer pico =
            new DefaultPicoContainer(parent, compFactory);
        pico.addComponent(NeedsStringBuilder.class, NeedsStringBuilderImpl.class);
        NeedsStringBuilder nsb = pico.getComponent(NeedsStringBuilder.class);
        assertNotNull(nsb);
        nsb.foo();
        nsb.foo();
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals(expectation, sb.toString());
    }

}