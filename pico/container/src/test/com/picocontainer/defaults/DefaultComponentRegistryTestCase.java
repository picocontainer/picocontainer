/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Stacy Curl                        *
 *****************************************************************************/

package com.picocontainer.defaults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import com.picocontainer.testmodel.AlternativeTouchable;
import com.picocontainer.testmodel.SimpleTouchable;
import com.picocontainer.testmodel.Touchable;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.monitors.NullComponentMonitor;

public class DefaultComponentRegistryTestCase {
    private DefaultPicoContainer picoContainer;

    @Before
    public void setUp() throws Exception {
        picoContainer = new DefaultPicoContainer();
    }

    @Test public void testRegisterComponent() throws PicoCompositionException {
        ComponentAdapter componentAdapter = createComponentAdapter();
        picoContainer.addAdapter(componentAdapter);
        assertTrue(picoContainer.getComponentAdapters().contains(componentAdapter));
    }

    @Test public void testUnregisterComponent() throws PicoCompositionException {
        ComponentAdapter componentAdapter = createComponentAdapter();
        picoContainer.addAdapter(componentAdapter);
        picoContainer.removeComponent(Touchable.class);
        assertFalse(picoContainer.getComponentAdapters().contains(componentAdapter));
    }

    @Test public void testCannotInstantiateAnUnregisteredComponent() throws PicoCompositionException {
        ComponentAdapter componentAdapter = createComponentAdapter();
        picoContainer.addAdapter(componentAdapter);
        picoContainer.getComponents();
        picoContainer.removeComponent(Touchable.class);

        assertTrue(picoContainer.getComponents().isEmpty());
    }

    @Test public void testCanInstantiateReplacedComponent() throws PicoCompositionException {
        ComponentAdapter componentAdapter = createComponentAdapter();
        picoContainer.addAdapter(componentAdapter);
        picoContainer.getComponents();
        picoContainer.removeComponent(Touchable.class);

        picoContainer.addComponent(Touchable.class, AlternativeTouchable.class);

        assertEquals("Container should container 1 addComponent",
                1, picoContainer.getComponents().size());
    }

    @Test public void testUnregisterAfterInstantiateComponents() throws PicoCompositionException {
        ComponentAdapter componentAdapter = createComponentAdapter();
        picoContainer.addAdapter(componentAdapter);
        picoContainer.getComponents();
        picoContainer.removeComponent(Touchable.class);
        assertNull(picoContainer.getComponent(Touchable.class));
    }

    @Test public void testReplacedInstantiatedComponentHasCorrectClass() throws PicoCompositionException {
        ComponentAdapter componentAdapter = createComponentAdapter();
        picoContainer.addAdapter(componentAdapter);
        picoContainer.getComponents();
        picoContainer.removeComponent(Touchable.class);

        picoContainer.addComponent(Touchable.class, AlternativeTouchable.class);
        Object component = picoContainer.getComponents().iterator().next();

        assertEquals(AlternativeTouchable.class, component.getClass());
    }

    private ComponentAdapter<SimpleTouchable> createComponentAdapter() throws PicoCompositionException {
        return new ConstructorInjection.ConstructorInjector<SimpleTouchable>(new NullComponentMonitor(), false,Touchable.class, SimpleTouchable.class, null);
    }
}
