/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.gems.monitors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.picocontainer.monitors.ComponentMonitorHelper.ctorToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.methodToString;
import static org.picocontainer.monitors.ComponentMonitorHelper.parmsToString;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.monitors.ComponentMonitorHelper;

/**
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 * @author Juze Peleteiro
 */
public abstract class ComponentMonitorHelperTestCase {
    private ComponentMonitor componentMonitor;
    private Constructor constructor;
    private Method method;
    
    @Before
    public void setUp() throws Exception {
        constructor = getConstructor();
        method = getMethod();
        componentMonitor = makeComponentMonitor();
    }

    protected abstract ComponentMonitor makeComponentMonitor();
    
    protected abstract Constructor getConstructor() throws NoSuchMethodException;

    protected abstract Method getMethod() throws NoSuchMethodException;
    
    protected abstract String getLogPrefix();

    protected void tearDown() throws Exception {
    	ForTestSakeAppender.CONTENT = "";
    }

    @Test public void testShouldTraceInstantiating() throws IOException {
        componentMonitor.instantiating(null, null, constructor);
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INSTANTIATING, ctorToString(constructor)));
    }

    @Test public void testShouldTraceInstantiatedWithInjected() throws IOException {
        Object[] injected = new Object[0];
        Object instantiated = new Object();
        componentMonitor.instantiated(null, null, constructor, instantiated, injected, 543);
        String s = ComponentMonitorHelper.format(ComponentMonitorHelper.INSTANTIATED, ctorToString(constructor), (long) 543, instantiated.getClass().getName(), parmsToString(injected));
        assertFileContent(getLogPrefix() + s);
    }


    @Test public void testShouldTraceInstantiationFailed() throws IOException {
        componentMonitor.instantiationFailed(null, null, constructor, new RuntimeException("doh"));
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INSTANTIATION_FAILED, ctorToString(constructor), "doh"));
    }

    @Test public void testShouldTraceInvoking() throws IOException {
        componentMonitor.invoking(null, null, method, this, new Object[] {"1","2"});
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INVOKING, methodToString(method), this));
    }

    @Test public void testShouldTraceInvoked() throws IOException {
        componentMonitor.invoked(null, null, method, this, 543, new Object[] {"1","2"}, "3");
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INVOKED, methodToString(method), this, (long) 543));
    }

    @Test public void testShouldTraceInvocatiationFailed() throws IOException {
        componentMonitor.invocationFailed(method, this, new RuntimeException("doh"));
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INVOCATION_FAILED, methodToString(method), this, "doh"));
    }

    @Test public void testShouldTraceNoComponent() throws IOException {
        componentMonitor.noComponentFound(null, "doh");
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.NO_COMPONENT, "doh"));
    }

    @Test public void shouldSerialize() throws IOException, ClassNotFoundException{
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutputStream oos = new ObjectOutputStream(bos);
    	oos.writeObject(componentMonitor);
    	oos.close();
    	
    	byte[] savedBytes = bos.toByteArray();
    	assertNotNull(savedBytes);
    	ByteArrayInputStream bis = new ByteArrayInputStream(savedBytes);
    	ObjectInputStream ois = new ObjectInputStream(bis);
    	this.componentMonitor = (ComponentMonitor) ois.readObject();
    	assertNotNull(componentMonitor);
    	//Run through a couple of other tests so we know that the delegate logger internally 
    	//appears to be working.    
    	testShouldTraceInstantiatedWithInjected();
    }
    

    protected void assertFileContent(final String line) throws IOException{
        List lines = toLines( new StringReader( ForTestSakeAppender.CONTENT ) );
        String s = lines.toString();
        assertTrue("Line '" + line + "' not found.  Instead got: " + line, s.indexOf(line) > 0);
    }
    
    protected List toLines(final Reader resource) throws IOException {
        BufferedReader br = new BufferedReader(resource);
        List lines = new ArrayList();
        String line = br.readLine();
        while ( line != null) {
            lines.add(line);
            line = br.readLine();
        }
        return lines;
    } 

}
