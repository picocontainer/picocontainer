/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import static org.picocontainer.Characteristics.GUARD;

import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.tck.AbstractComponentFactoryTest;


/**
 * @author Paul Hammant
 */
public class GuardingTestCase extends AbstractComponentFactoryTest {

    protected ComponentFactory createComponentFactory() {
        return new Guarding().wrap(new ConstructorInjection());
    }

    @Test public void testAddComponentUsesGuardingBehavior() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), createComponentFactory());
        pico.addComponent("guard", MyGuard.class);
        pico.as(GUARD).addComponent("foo", String.class);
        ComponentAdapter fooAdapter = pico.getComponentAdapter("foo");
        assertEquals(Guarding.Guarded.class, fooAdapter.getClass());
        assertEquals(ConstructorInjection.ConstructorInjector.class, ((AbstractBehavior.AbstractChangedBehavior) fooAdapter).getDelegate().getClass());
        try {
            String foo = (String) pico.getComponent("foo");
            fail("should have barfed");
        } catch (Exception e) {
            assertEquals("not so fast", e.getMessage());
        }
    }

    public static class MyGuard {
        public MyGuard() {
            throw new RuntimeException("not so fast");
        }
    }

    @Test public void testAddComponentDoesNotUseGuardingBehaviorIfNoProperty() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), createComponentFactory());
        pico.addComponent("guard", MyGuard.class);
        pico.addComponent("foo", String.class);
        ComponentAdapter fooAdapter = pico.getComponentAdapter("foo");
        assertEquals(ConstructorInjection.ConstructorInjector.class, fooAdapter.getClass());
        String foo = (String) pico.getComponent("foo");
    }

    @Test public void testAddComponentUsesGuardingBehaviorWithCustomGuardKey() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy(), createComponentFactory());
        pico.addComponent("my_guard", MyGuard.class);
        pico.as(GUARD("my_guard")).addComponent("foo", String.class);
        ComponentAdapter fooAdapter = pico.getComponentAdapter("foo");
        assertEquals(Guarding.Guarded.class, fooAdapter.getClass());
        assertEquals(ConstructorInjection.ConstructorInjector.class, fooAdapter.getDelegate().getClass());
        try {
            String foo = (String) pico.getComponent("foo");
            fail("should have barfed");
        } catch (Exception e) {
            assertEquals("not so fast", e.getMessage());
        }
    }

    @Test public void testAddComponentUsesGuardingBehaviorByAdapitveDefault() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new NullLifecycleStrategy());
        pico.addComponent("guard", MyGuard.class);
        pico.as(GUARD).addComponent("foo", String.class);
        ComponentAdapter fooAdapter = pico.getComponentAdapter("foo");
        assertEquals(Guarding.Guarded.class, fooAdapter.getClass());
        assertEquals(ConstructorInjection.ConstructorInjector.class, fooAdapter.getDelegate().getClass());
        try {
            String foo = (String) pico.getComponent("foo");
            fail("should have barfed");
        } catch (Exception e) {
            assertEquals("not so fast", e.getMessage());
        }
    }

}