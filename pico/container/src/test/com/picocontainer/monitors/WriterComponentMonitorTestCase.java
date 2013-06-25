/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.monitors;

import static com.picocontainer.monitors.ComponentMonitorHelper.ctorToString;
import static com.picocontainer.monitors.ComponentMonitorHelper.format;
import static com.picocontainer.monitors.ComponentMonitorHelper.memberToString;
import static com.picocontainer.monitors.ComponentMonitorHelper.methodToString;
import static com.picocontainer.monitors.ComponentMonitorHelper.parmsToString;
import static org.junit.Assert.assertEquals;

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

import com.picocontainer.ComponentMonitor;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.PicoLifecycleException;
import com.picocontainer.adapters.AbstractAdapter;
import com.picocontainer.containers.TransientPicoContainer;
import com.picocontainer.monitors.ComponentMonitorHelper;
import com.picocontainer.monitors.WriterComponentMonitor;

/**
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class WriterComponentMonitorTestCase  {

    private Writer out;
    private ComponentMonitor monitor;
    private static final String NL = System.getProperty("line.separator");
    private Constructor constructor;
    private Method method;

    @Before
    public void setUp() throws Exception {
        out = new StringWriter();
        constructor = getClass().getConstructor((Class[])null);
        method = getClass().getDeclaredMethod("setUp", (Class[])null);
        monitor = new WriterComponentMonitor(out);
    }

    @SuppressWarnings("unchecked")
    @Test public void testShouldTraceInstantiating() {
        monitor.instantiating(null, null, constructor);
        assertEquals(format(ComponentMonitorHelper.INSTANTIATING, ctorToString(constructor)) +NL,  out.toString());
    }

    @SuppressWarnings("unchecked")
    @Test public void testShouldTraceInstantiatedWithInjected() {
        Object[] injected = new Object[0];
        Object instantiated = new Object();
        monitor.instantiated(null, null, constructor, instantiated, injected, 543);
        Assert.assertEquals(format(ComponentMonitorHelper.INSTANTIATED,
                                                   ctorToString(constructor),
                                                   (long)543,
                                                   instantiated.getClass().getName(), parmsToString(injected)) +NL,  out.toString());
    }

    @SuppressWarnings("unchecked")
    @Test public void testShouldTraceInstantiationFailed() {
        monitor.instantiationFailed(null, null, constructor, new RuntimeException("doh"));
        Assert.assertEquals(format(ComponentMonitorHelper.INSTANTIATION_FAILED,
                                                   ctorToString(constructor), "doh") +NL,  out.toString());
    }

    @Test public void testShouldTraceInvoking() {
        monitor.invoking(null, null, method, this, new Object[0]);
        Assert.assertEquals(format(ComponentMonitorHelper.INVOKING,
                                                   memberToString(method), this) +NL,  out.toString());
    }

    @Test public void testShouldTraceInvoked() {
        monitor.invoked(null, null, method, this, 543, null, new Object[0]);
        Assert.assertEquals(format(ComponentMonitorHelper.INVOKED,
                                                   methodToString(method), this,
                                                   (long)543) +NL,  out.toString());
    }

    @Test public void testShouldTraceInvocatiationFailed() {
        monitor.invocationFailed(method, this, new RuntimeException("doh"));
        Assert.assertEquals(format(ComponentMonitorHelper.INVOCATION_FAILED,
        		memberToString(method), this, "doh") +NL,  out.toString());
    }

    @SuppressWarnings("unchecked")
    @Test public void testShouldTraceLifecycleInvocationFailed() {
        try {
            monitor.lifecycleInvocationFailed(new TransientPicoContainer(),
                                                       new AbstractAdapter(Map.class, HashMap.class) {

                                                           public Object getComponentInstance(final PicoContainer container, final Type into)
                                                               throws PicoCompositionException {
                                                               return "x";
                                                           }

                                                           public void verify(final PicoContainer container)
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

        monitor.noComponentFound(new TransientPicoContainer(), "foo");
        Assert.assertEquals(format(ComponentMonitorHelper.NO_COMPONENT,
                                                   "foo") +NL,  out.toString());
    }


}
