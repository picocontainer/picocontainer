/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ConstructorInjector;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.tck.AbstractComponentFactoryTest;


/**
 * @author <a href="Rafal.Krzewski">rafal@caltha.pl</a>
 */
public class OptInCachingTestCase extends AbstractComponentFactoryTest {

    protected ComponentFactory createComponentFactory() {
        return new OptInCaching().wrap(new ConstructorInjection());
    }

    @Test public void testAddComponentDoesNotUseCachingBehaviorByDefault() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new OptInCaching().wrap(new ConstructorInjection()), new NullLifecycleStrategy(), new EmptyPicoContainer());
        pico.addComponent("foo", String.class);
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(ConstructorInjector.class, foo.getClass());
    }

    @Test public void testAddComponentUsesOptinBehaviorWithRedundantCacheProperty() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new OptInCaching().wrap(new ConstructorInjection()), new NullLifecycleStrategy(), new EmptyPicoContainer());
        pico.change(Characteristics.CACHE).addComponent("foo", String.class);
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(Cached.class, foo.getClass());
        assertEquals(ConstructorInjector.class, ((AbstractBehavior) foo).getDelegate().getClass());
    }

    @Test public void testAddComponentNoesNotUseOptinBehaviorWhenNoCachePropertyIsSpecified() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new OptInCaching().wrap(new ConstructorInjection()), new NullLifecycleStrategy(), new EmptyPicoContainer());
        pico.change(Characteristics.NO_CACHE).addComponent("foo", String.class);
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(ConstructorInjector.class, foo.getClass());
    }

    @Test public void testAddAdapterUsesDoesNotUseCachingBehaviorByDefault() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new OptInCaching().wrap(new ConstructorInjection()));
        pico.addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(InstanceAdapter.class, foo.getClass());
    }

    @Test public void testAddAdapterUsesCachingBehaviorWithHideImplProperty() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new OptInCaching().wrap(new ConstructorInjection()));
        pico.change(Characteristics.CACHE).addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(Cached.class, foo.getClass());
        assertEquals(InstanceAdapter.class, ((AbstractBehavior) foo).getDelegate().getClass());
    }

    @Test public void testAddAdapterNoesNotUseImplementationHidingBehaviorWhenNoCachePropertyIsSpecified() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new OptInCaching().wrap(new ConstructorInjection()));
        pico.change(Characteristics.NO_CACHE).addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(InstanceAdapter.class, foo.getClass());
    }



}