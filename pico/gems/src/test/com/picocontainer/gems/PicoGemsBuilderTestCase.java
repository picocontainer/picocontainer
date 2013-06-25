/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.gems;

import static org.junit.Assert.assertEquals;
import static com.picocontainer.gems.PicoGemsBuilder.ASM_IMPL_HIDING;
import static com.picocontainer.gems.PicoGemsBuilder.LOG4J;

import org.junit.Before;
import org.junit.Test;
import com.picocontainer.gems.behaviors.AsmImplementationHiding;
import com.picocontainer.gems.monitors.CommonsLoggingComponentMonitor;
import com.picocontainer.gems.monitors.Log4JComponentMonitor;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.AdaptingInjection;
import com.picocontainer.lifecycle.NullLifecycleStrategy;
import com.picocontainer.monitors.NullComponentMonitor;
import com.thoughtworks.xstream.XStream;

public class PicoGemsBuilderTestCase {

    private XStream xs;
    private final NullLifecycleStrategy lifecycle = new NullLifecycleStrategy();
    private final EmptyPicoContainer parent = new EmptyPicoContainer();

    @Before
    public void setUp() throws Exception {
        xs = new XStream();
        xs.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    }

    @Test public void testWithImplementationHiding() {
        MutablePicoContainer actual = new PicoBuilder().withBehaviors(ASM_IMPL_HIDING()).build();
        MutablePicoContainer expected = new DefaultPicoContainer(parent, lifecycle, new NullComponentMonitor(), new AsmImplementationHiding().wrap(new AdaptingInjection())
       );
        assertEquals(xs.toXML(expected), xs.toXML(actual));
    }

    @Test public void testWithLog4JComponentMonitor() {
        MutablePicoContainer actual = new PicoBuilder().withMonitor(Log4JComponentMonitor.class).build();
        MutablePicoContainer expected = new DefaultPicoContainer(parent, lifecycle, new Log4JComponentMonitor(), new AdaptingInjection()
       );
        assertEquals(xs.toXML(expected), xs.toXML(actual));
    }

    @Test public void testWithLog4JComponentMonitorByInstance() {
        MutablePicoContainer actual = new PicoBuilder().withMonitor(LOG4J()).build();
        MutablePicoContainer expected = new DefaultPicoContainer(parent, lifecycle, new Log4JComponentMonitor(), new AdaptingInjection()
       );
        assertEquals(xs.toXML(expected), xs.toXML(actual));
    }

    @Test public void testWithCommonsLoggingComponentMonitor() {
        MutablePicoContainer actual = new PicoBuilder().withMonitor(CommonsLoggingComponentMonitor.class).build();
        MutablePicoContainer expected = new DefaultPicoContainer(parent, lifecycle, new CommonsLoggingComponentMonitor(), new AdaptingInjection()
       );
        assertEquals(xs.toXML(expected), xs.toXML(actual));
    }

}
