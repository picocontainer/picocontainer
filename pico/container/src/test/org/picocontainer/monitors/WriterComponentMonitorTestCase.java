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

import static org.junit.Assert.assertEquals;
import static org.picocontainer.monitors.ComponentMonitorHelper.ctorToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.format;
import static org.picocontainer.monitors.ComponentMonitorHelper.methodToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.parmsToString;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoLifecycleException;
import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.containers.TransientPicoContainer;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class WriterComponentMonitorTestCase  {
	
    private Writer out;
    private ComponentMonitor componentMonitor;
    private static final String NL = System.getProperty("line.separator");
    private Constructor constructor;
    private Method method;

    @Before
    public void setUp() throws Exception {
        out = new StringWriter();
        constructor = getClass().getConstructor((Class[])null);
        method = getClass().getDeclaredMethod("setUp", (Class[])null);
        componentMonitor = new WriterComponentMonitor(out);
    }

    @SuppressWarnings("unchecked")
    @Test public void testShouldTraceInstantiating() {
        componentMonitor.instantiating(null, null, constructor);
        assertEquals(format(ComponentMonitorHelper.INSTANTIATING, ctorToString(constructor)) +NL,  out.toString());
    }

    @SuppressWarnings("unchecked")
    @Test public void testShouldTraceInstantiatedWithInjected() {
        Object[] injected = new Object[0];
        Object instantiated = new Object();
        componentMonitor.instantiated(null, null, constructor, instantiated, injected, 543);
        Assert.assertEquals(format(ComponentMonitorHelper.INSTANTIATED,
                                                   ctorToString(constructor),
                                                   (long)543,
                                                   instantiated.getClass().getName(), parmsToString(injected)) +NL,  out.toString());
    }

    @SuppressWarnings("unchecked")
    @Test public void testShouldTraceInstantiationFailed() {
        componentMonitor.instantiationFailed(null, null, constructor, new RuntimeException("doh"));
        Assert.assertEquals(format(ComponentMonitorHelper.INSTANTIATION_FAILED,
                                                   ctorToString(constructor), "doh") +NL,  out.toString());
    }

    @Test public void testShouldTraceInvoking() {
        componentMonitor.invoking(null, null, method, this, new Object[0]);
        Assert.assertEquals(format(ComponentMonitorHelper.INVOKING,
                                                   methodToString(method), this) +NL,  out.toString());
    }

    @Test public void testShouldTraceInvoked() {
        componentMonitor.invoked(null, null, method, this, 543, new Object[0], null);
        Assert.assertEquals(format(ComponentMonitorHelper.INVOKED,
                                                   methodToString(method), this,
                                                   (long)543) +NL,  out.toString());
    }

    @Test public void testShouldTraceInvocatiationFailed() {
        componentMonitor.invocationFailed(method, this, new RuntimeException("doh"));
        Assert.assertEquals(format(ComponentMonitorHelper.INVOCATION_FAILED,
                                                   methodToString(method), this, "doh") +NL,  out.toString());
    }

    @SuppressWarnings("unchecked")
    @Test public void testShouldTraceLifecycleInvocationFailed() {
        try {
            componentMonitor.lifecycleInvocationFailed(new TransientPicoContainer(),
                                                       new AbstractAdapter(Map.class, HashMap.class) {

                                                           public Object getComponentInstance(PicoContainer container, Type into)
                                                               throws PicoCompositionException {
                                                               return "x";
                                                           }

                                                           public void verify(PicoContainer container)
                                                               throws PicoCompositionException{
                                                           }

                                                           public String getDescriptor() {
                                                               return null;
                                                           }
                                                       },
                                                       method,
                                                       "fooooo",
                                                       new RuntimeException("doh"));
            Assert.fail("should have barfed");
        } catch (PicoLifecycleException e) {
            //expected
        }
        Assert.assertEquals(format(ComponentMonitorHelper.LIFECYCLE_INVOCATION_FAILED,
                                                   methodToString(method), "fooooo", "doh") + NL,
                     out.toString());
    }

    @Test public void testNoComponent() {
        
        componentMonitor.noComponentFound(new TransientPicoContainer(), "foo");
        Assert.assertEquals(format(ComponentMonitorHelper.NO_COMPONENT,
                                                   "foo") +NL,  out.toString());
    }


}
