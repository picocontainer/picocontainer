/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.ConsoleComponentMonitor;

public class TypedFieldInjectionTestCase {
    private static final String FIELD_TYPES = Integer.class.getName() + " " + PogoStick.class.getName() + " " + Float.class.getName();

    public static class Helicopter {
        private PogoStick pogo;
    }

    public static class PogoStick {
    }


    @Test public void testFactoryMakesNamedInjector() {

        TypedFieldInjection injectionFactory = new TypedFieldInjection();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        Properties props = new Properties();
        props.setProperty("injectionFieldTypes", FIELD_TYPES);
        ComponentAdapter ca = injectionFactory.createComponentAdapter(cm, new NullLifecycleStrategy(),
                props, Map.class, HashMap.class, Parameter.DEFAULT);

        assertTrue(ca instanceof TypedFieldInjection.TypedFieldInjector);

        TypedFieldInjection.TypedFieldInjector tfi = (TypedFieldInjection.TypedFieldInjector) ca;

        assertEquals(3, tfi.getInjectionFieldTypes().size());
        assertEquals(Integer.class.getName(), tfi.getInjectionFieldTypes().get(0));
        assertEquals(PogoStick.class.getName(), tfi.getInjectionFieldTypes().get(1));
        assertEquals(Float.class.getName(), tfi.getInjectionFieldTypes().get(2));
    }

    @Test public void testPropertiesAreRight() {
        Properties props = TypedFieldInjection.injectionFieldTypes(FIELD_TYPES);
        assertEquals("java.lang.Integer org.picocontainer.injectors.TypedFieldInjectionTestCase$PogoStick java.lang.Float", props.getProperty("injectionFieldTypes"));
        assertEquals(1, props.size());
    }


}