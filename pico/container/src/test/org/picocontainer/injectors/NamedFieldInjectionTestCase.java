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
import org.picocontainer.Characteristics;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.ConsoleComponentMonitor;

public class NamedFieldInjectionTestCase {

    @Test public void testFactoryMakesNamedInjector() {

        NamedFieldInjection injectionFactory = new NamedFieldInjection();

        ConsoleComponentMonitor cm = new ConsoleComponentMonitor();
        Properties props = new Properties();
        props.setProperty("injectionFieldNames", " aa pogo bb ");
        ComponentAdapter ca = injectionFactory.createComponentAdapter(cm, new NullLifecycleStrategy(),
                props, Map.class, HashMap.class, Parameter.DEFAULT);
        
        assertTrue(ca instanceof NamedFieldInjection.NamedFieldInjector);

        NamedFieldInjection.NamedFieldInjector nfi = (NamedFieldInjection.NamedFieldInjector) ca;

        assertEquals(3, nfi.getInjectionFieldNames().size());
        assertEquals("pogo", nfi.getInjectionFieldNames().get(1));
    }

    @Test public void testPropertiesAreRight() {
        Properties props = NamedFieldInjection.injectionFieldNames("aa","pogo","bb");
        assertTrue(props instanceof Characteristics.ImmutableProperties);
        assertEquals("aa pogo bb", props.getProperty("injectionFieldNames"));
        assertEquals(1, props.size());
    }


}
