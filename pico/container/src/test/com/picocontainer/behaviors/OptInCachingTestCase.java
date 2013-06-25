/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.behaviors;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import com.picocontainer.tck.AbstractComponentFactoryTest;

import com.picocontainer.Characteristics;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentFactory;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.adapters.InstanceAdapter;
import com.picocontainer.behaviors.AbstractBehavior;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.OptInCaching;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;


/**
 * @author <a href="Rafal.Krzewski">rafal@caltha.pl</a>
 */
public class OptInCachingTestCase extends AbstractComponentFactoryTest {

    @Override
	protected ComponentFactory createComponentFactory() {
        return new OptInCaching().wrap(new ConstructorInjection());
    }

    @Test public void testAddComponentDoesNotUseCachingBehaviorByDefault() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new OptInCaching().wrap(new ConstructorInjection()));
        pico.addComponent("foo", String.class);
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(ConstructorInjection.ConstructorInjector.class, foo.getClass());
    }

    @Test public void testAddComponentUsesOptinBehaviorWithRedundantCacheProperty() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new OptInCaching().wrap(new ConstructorInjection()));
        pico.change(Characteristics.CACHE).addComponent("foo", String.class);
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(Caching.Cached.class, foo.getClass());
        assertEquals(ConstructorInjection.ConstructorInjector.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());
    }

    @Test public void testAddComponentNoesNotUseOptinBehaviorWhenNoCachePropertyIsSpecified() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), new OptInCaching().wrap(new ConstructorInjection()));
        pico.change(Characteristics.NO_CACHE).addComponent("foo", String.class);
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(ConstructorInjection.ConstructorInjector.class, foo.getClass());
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
        assertEquals(Caching.Cached.class, foo.getClass());
        assertEquals(InstanceAdapter.class, ((AbstractBehavior.AbstractChangedBehavior) foo).getDelegate().getClass());
    }

    @Test public void testAddAdapterNoesNotUseImplementationHidingBehaviorWhenNoCachePropertyIsSpecified() {
        DefaultPicoContainer pico =
            new DefaultPicoContainer(new OptInCaching().wrap(new ConstructorInjection()));
        pico.change(Characteristics.NO_CACHE).addAdapter(new InstanceAdapter("foo", "bar", new NullLifecycleStrategy(), new NullComponentMonitor()));
        ComponentAdapter foo = pico.getComponentAdapter("foo");
        assertEquals(InstanceAdapter.class, foo.getClass());
    }



}