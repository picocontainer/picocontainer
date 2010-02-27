/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.monitors;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentMonitor;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
public class ConsoleComponentMonitorTestCase {
    private ComponentMonitor componentMonitor;
    private Constructor constructor;
    private Method method;

    @Before
    public void setUp() throws Exception {
        PrintStream out = System.out;
        constructor = getClass().getConstructor((Class[])null);
        method = getClass().getDeclaredMethod("setUp", (Class[])null);
        componentMonitor = new ConsoleComponentMonitor(out);
    }

    @Test public void testShouldTraceInstantiating() {
        componentMonitor.instantiating(null, null, constructor);
    }

    @Test public void testShouldTraceInstantiatedWithInjected() {
        componentMonitor.instantiated(null, null, constructor, new Object(), new Object[0], 543);
    }

    @Test public void testShouldTraceInstantiationFailed() {
        componentMonitor.instantiationFailed(null, null, constructor, new RuntimeException("doh"));
    }

    @Test public void testShouldTraceInvoking() {
        componentMonitor.invoking(null, null, method, this, new Object[0]);
    }

    @Test public void testShouldTraceInvoked() {
        componentMonitor.invoked(null, null, method, this, 543, new Object[0], null);
    }

    @Test public void testShouldTraceInvocatiationFailed() {
        componentMonitor.invocationFailed(method, this, new RuntimeException("doh"));
    }

}
