/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.behaviors;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import com.picocontainer.tck.AbstractComponentFactoryTest;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.adapters.InstanceAdapter;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.ImplementationHiding;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.AdaptingInjection;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;


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

    @Override
	protected ComponentFactory createComponentFactory() {
        return implementationHidingComponentFactory;
    }


    public static interface NeedsStringBuilder {
        void foo();
    }
    public static class NeedsStringBuilderImpl implements NeedsStringBuilder {
        StringBuilder sb;

        public NeedsStringBuilderImpl(final StringBuilder sb) {
            this.sb = sb;
            sb.append("<init>");
        }
        public void foo() {
            sb.append("foo()");
        }
    }
    public static class NeedsNeedsStringBuilder {

        NeedsStringBuilder nsb;

        public NeedsNeedsStringBuilder(final NeedsStringBuilder nsb) {
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

    @Test public void shouldInstantiateForEveryGetComponentCall() {
        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        parent.addComponent(StringBuilder.class);
        DefaultPicoContainer pico =
            new DefaultPicoContainer(parent, new ImplementationHiding());
        pico.addComponent(NeedsStringBuilder.class, NeedsStringBuilderImpl.class);
        NeedsStringBuilder nsb = pico.getComponent(NeedsStringBuilder.class);
        nsb.foo();
        nsb = pico.getComponent(NeedsStringBuilder.class);
        nsb.foo();
        StringBuilder sb = pico.getComponent(StringBuilder.class);
        assertEquals("<init>foo()<init>foo()", sb.toString());
    }

    @Test public void shouldInstantiateForEveryMethodCall() {
        cachingTestBody("<init>foo()foo()", new ImplementationHiding());
    }

    private void cachingTestBody(final String expectation, final ComponentFactory compFactory) {
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