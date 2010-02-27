/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.gems;

import com.thoughtworks.xstream.XStream;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.gems.behaviors.AsmImplementationHiding;
import org.picocontainer.gems.monitors.CommonsLoggingComponentMonitor;
import org.picocontainer.gems.monitors.Log4JComponentMonitor;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;

import static org.junit.Assert.assertEquals;
import static org.picocontainer.gems.PicoGemsBuilder.IMPL_HIDING;
import static org.picocontainer.gems.PicoGemsBuilder.LOG4J;

public class PicoGemsBuilderTestCase {

    private XStream xs;
    private NullLifecycleStrategy lifecycleStrategy = new NullLifecycleStrategy();
    private EmptyPicoContainer parent = new EmptyPicoContainer();

    @Before
    public void setUp() throws Exception {
        xs = new XStream();
        xs.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
    }

    @Test public void testWithImplementationHiding() {
        MutablePicoContainer actual = new PicoBuilder().withBehaviors(IMPL_HIDING()).build();
        MutablePicoContainer expected = new DefaultPicoContainer(new AsmImplementationHiding().wrap(new AdaptingInjection()),
                lifecycleStrategy, parent, new NullComponentMonitor());
        assertEquals(xs.toXML(expected), xs.toXML(actual));
    }

    @Test public void testWithLog4JComponentMonitor() {
        MutablePicoContainer actual = new PicoBuilder().withMonitor(Log4JComponentMonitor.class).build();
        MutablePicoContainer expected = new DefaultPicoContainer(new AdaptingInjection(),
                lifecycleStrategy, parent, new Log4JComponentMonitor());
        assertEquals(xs.toXML(expected), xs.toXML(actual));
    }

    @Test public void testWithLog4JComponentMonitorByInstance() {
        MutablePicoContainer actual = new PicoBuilder().withMonitor(LOG4J()).build();
        MutablePicoContainer expected = new DefaultPicoContainer(new AdaptingInjection(),
                lifecycleStrategy, parent, new Log4JComponentMonitor());
        assertEquals(xs.toXML(expected), xs.toXML(actual));
    }

    @Test public void testWithCommonsLoggingComponentMonitor() {
        MutablePicoContainer actual = new PicoBuilder().withMonitor(CommonsLoggingComponentMonitor.class).build();
        MutablePicoContainer expected = new DefaultPicoContainer(new AdaptingInjection(),
                lifecycleStrategy, parent, new CommonsLoggingComponentMonitor());
        assertEquals(xs.toXML(expected), xs.toXML(actual));
    }

}
