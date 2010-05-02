/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaibe                                            *
 *****************************************************************************/

package org.picocontainer.gems.behaviors;

import com.thoughtworks.proxy.factory.CglibProxyFactory;
import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.ConstructorInjector;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.tck.AbstractComponentAdapterTest;
import org.picocontainer.testmodel.CompatibleTouchable;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * @author J&ouml;rg Schaible
 */
public class AssimilatedTestCase extends AbstractComponentAdapterTest {

    /**
     * Test if an instance can be assimilated.
     */
    @Test public void testInstanceIsBorgedAndCompatibleWithGenerics() {
        final MutablePicoContainer mpc = new DefaultPicoContainer();
        final ComponentAdapter<Touchable> componentAdapter = new Caching.Cached<Touchable>(new ConstructorInjector<Touchable>(
                CompatibleTouchable.class, CompatibleTouchable.class, null, new NullComponentMonitor(), false));
        mpc.addAdapter(new Assimilating.Assimilated<Touchable>(Touchable.class, componentAdapter));
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
        final ComponentAdapter<Touchable> componentAdapter = new Caching.Cached<Touchable>(new ConstructorInjector<Touchable>(
                "Touchy", CompatibleTouchable.class, null, new NullComponentMonitor(), false));
        mpc.addAdapter(new Assimilating.Assimilated<Touchable>(Touchable.class, componentAdapter));
        final CompatibleTouchable compatibleTouchable = (CompatibleTouchable) componentAdapter.getComponentInstance(mpc,null);
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
        return new Assimilating.Assimilated(Touchable.class, new ConstructorInjector(
                CompatibleTouchable.class, CompatibleTouchable.class, null, new NullComponentMonitor(), false));
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
