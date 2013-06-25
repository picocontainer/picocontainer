/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package com.picocontainer.gems.behaviors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Test;
import com.picocontainer.tck.AbstractComponentAdapterTest;
import com.picocontainer.testmodel.CompatibleTouchable;
import com.picocontainer.testmodel.SimpleTouchable;
import com.picocontainer.testmodel.Touchable;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.adapters.InstanceAdapter;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;
import com.thoughtworks.proxy.factory.CglibProxyFactory;


/**
 * @author J&ouml;rg Schaible
 */
public class AssimilatedTestCase extends AbstractComponentAdapterTest {

    /**
     * Test if an instance can be assimilated.
     */
    @Test public void testInstanceIsBorgedAndCompatibleWithGenerics() {
        final MutablePicoContainer mpc = new DefaultPicoContainer();
        final ComponentAdapter componentAdapter = new Caching.Cached<CompatibleTouchable>(
                new ConstructorInjection.ConstructorInjector<CompatibleTouchable>(CompatibleTouchable.class, CompatibleTouchable.class));
        mpc.addAdapter(new Assimilating.Assimilated(Touchable.class, componentAdapter));
        final CompatibleTouchable compatibleTouchable = (CompatibleTouchable) componentAdapter.getComponentInstance(mpc,null);
        final Touchable touchable = mpc.getComponent(Touchable.class);
        assertFalse(compatibleTouchable.wasTouched());
        touchable.touch();
        assertTrue(compatibleTouchable.wasTouched());
        assertTrue(Proxy.isProxyClass(touchable.getClass()));
    }

    /**
     * Test if the component key is preserved if it is not a class type.
     */
    @Test public void testComponentKeyIsPreserved() {
        final MutablePicoContainer mpc = new DefaultPicoContainer();
        final ComponentAdapter<CompatibleTouchable> componentAdapter = new Caching.Cached<CompatibleTouchable>(
                new ConstructorInjection.ConstructorInjector<CompatibleTouchable>("Touchy", CompatibleTouchable.class));
        mpc.addAdapter(new Assimilating.Assimilated(Touchable.class, componentAdapter));
        final CompatibleTouchable compatibleTouchable = componentAdapter.getComponentInstance(mpc, null);
        final Touchable touchable = (Touchable)mpc.getComponent("Touchy");
        assertFalse(compatibleTouchable.wasTouched());
        touchable.touch();
        assertTrue(compatibleTouchable.wasTouched());
        assertTrue(Proxy.isProxyClass(touchable.getClass()));
    }

    /**
     * Test if proxy generation is omitted, if types are compatible.
     */
    @Test public void testAvoidUnnecessaryProxy() {
        final MutablePicoContainer mpc = new DefaultPicoContainer();
        mpc.addAdapter(new Assimilating.Assimilated<AbstractComponentAdapterTest>(AbstractComponentAdapterTest.class, new InstanceAdapter<AbstractComponentAdapterTest>(AbstractComponentAdapterTest.class, this, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor())));
        final AbstractComponentAdapterTest self = mpc.getComponent(AbstractComponentAdapterTest.class);
        assertFalse(Proxy.isProxyClass(self.getClass()));
        assertSame(this, self);
    }

    /**
     * Test if proxy generation is omitted, if types are compatible and that the component key is not changed.
     */
    @Test public void testAvoidedProxyDoesNotChangeComponentKey() {
        final MutablePicoContainer mpc = new DefaultPicoContainer();
        mpc.addAdapter(new Assimilating.Assimilated<AssimilatedTestCase>(AssimilatedTestCase.class, new InstanceAdapter<AssimilatedTestCase>(getClass(), this, new NullLifecycleStrategy(),
                new NullComponentMonitor())));
        final AbstractComponentAdapterTest self = mpc.getComponent(getClass());
        assertNotNull(self);
        assertSame(this, self);
    }

    /**
     * Test fail-fast for components without interface.
     */
    @Test public void testComponentMustImplementInterface() {
        try {
            new Assimilating.Assimilated(SimpleTouchable.class, new InstanceAdapter<AssimilatedTestCase>(AbstractComponentAdapterTest.class, this, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor()));
            fail("PicoCompositionException expected");
        } catch (final PicoCompositionException e) {
            assertTrue(e.getMessage().endsWith(SimpleTouchable.class.getName()));
        }
    }

    /**
     * Test fail-fast for components without matching methods.
     * @throws NoSuchMethodException
     */
    @Test public void testComponentMustHaveMathichMethods() throws NoSuchMethodException {
        final Method touch = Touchable.class.getMethod("touch", (Class[])null);
        try {
            new Assimilating.Assimilated(Touchable.class, new InstanceAdapter<AssimilatedTestCase>(AbstractComponentAdapterTest.class, this, new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor()));
            fail("PicoCompositionException expected");
        } catch (final PicoCompositionException e) {
            assertTrue(e.getMessage().endsWith(touch.toString()));
        }
    }

    // -------- TCK -----------

    @Override
	protected Class getComponentAdapterType() {
        return Assimilating.Assimilated.class;
    }

    @Override
	protected int getComponentAdapterNature() {
        return super.getComponentAdapterNature() & ~(RESOLVING | VERIFYING | INSTANTIATING);
    }

    private ComponentAdapter createComponentAdapterWithTouchable() {
        return new Assimilating.Assimilated(Touchable.class, new ConstructorInjection.ConstructorInjector(
                CompatibleTouchable.class, CompatibleTouchable.class));
    }

    @Override
	protected ComponentAdapter prepDEF_verifyWithoutDependencyWorks(final MutablePicoContainer picoContainer) {
        return createComponentAdapterWithTouchable();
    }

    @Override
	protected ComponentAdapter prepDEF_verifyDoesNotInstantiate(final MutablePicoContainer picoContainer) {
        return createComponentAdapterWithTouchable();
    }

    @Override
	protected ComponentAdapter prepDEF_visitable() {
        return createComponentAdapterWithTouchable();
    }

    @Override
	protected ComponentAdapter prepSER_isSerializable(final MutablePicoContainer picoContainer) {
        return new Assimilating.Assimilated(Touchable.class, new InstanceAdapter<CompatibleTouchable>(
                CompatibleTouchable.class, new CompatibleTouchable(), new NullLifecycleStrategy(),
                                                                        new NullComponentMonitor()), new CglibProxyFactory());
    }

    @Override
	protected ComponentAdapter prepSER_isXStreamSerializable(final MutablePicoContainer picoContainer) {
        return createComponentAdapterWithTouchable();
    }
}
