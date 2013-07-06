/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package com.picocontainer.gems.monitors;

import static com.picocontainer.monitors.ComponentMonitorHelper.ctorToString;
import static com.picocontainer.monitors.ComponentMonitorHelper.memberToString;
import static com.picocontainer.monitors.ComponentMonitorHelper.parmsToString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.runner.RunWith;

import com.picocontainer.ChangedBehavior;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.PicoLifecycleException;
import com.picocontainer.monitors.ComponentMonitorHelper;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;

/**
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Mauro Talevi
 * @author Juze Peleteiro
 */
@RunWith(JMock.class)
public abstract class ComponentMonitorHelperTestCase {
    private ComponentMonitor monitor;
    private Constructor<?> constructor;
    private Method method;
    
    private Mockery context = new JUnit4Mockery();
    
    private ComponentMonitor delegate;

    @Before
    public void setUp() throws Exception {
        constructor = getConstructor();
        method = getMethod();
        monitor = makeComponentMonitor();
        delegate = context.mock(ComponentMonitor.class);
    }

    protected abstract ComponentMonitor makeComponentMonitor();
    
    protected abstract ComponentMonitor makeComponentMonitorWithDelegate(ComponentMonitor delegate);

    protected abstract Constructor<?> getConstructor() throws NoSuchMethodException;

    protected abstract Method getMethod() throws NoSuchMethodException;

    protected abstract String getLogPrefix();

    protected void tearDown() throws Exception {
    	ForTestSakeAppender.CONTENT = "";
    }

	@Test
	public void testShouldTraceInstantiating() throws IOException {
        monitor.instantiating(null, null, constructor);
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INSTANTIATING, ctorToString(constructor)));
    }

    @Test
    public void testShouldTraceInstantiatedWithInjected() throws IOException {
        Object[] injected = new Object[0];
        Object instantiated = new Object();
        monitor.instantiated(null, null, constructor, instantiated, injected, 543);
        String s = ComponentMonitorHelper.format(ComponentMonitorHelper.INSTANTIATED, ctorToString(constructor), (long) 543, instantiated.getClass().getName(), parmsToString(injected));
        assertFileContent(getLogPrefix() + s);
    }


    @Test
    public void testShouldTraceInstantiationFailed() throws IOException {
        monitor.instantiationFailed(null, null, constructor, new RuntimeException("doh"));
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INSTANTIATION_FAILED, ctorToString(constructor), "doh"));
    }

    @Test public void testShouldTraceInvoking() throws IOException {
        monitor.invoking(null, null, method, this, new Object[] {"1","2"});
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INVOKING, memberToString(method), this));
    }

    @Test public void testShouldTraceInvoked() throws IOException {
        monitor.invoked(null, null, method, this, 543, "3", new Object[] {"1","2"});
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INVOKED, memberToString(method), this, (long) 543));
    }

    @Test public void testShouldTraceInvocatiationFailed() throws IOException {
        monitor.invocationFailed(method, this, new RuntimeException("doh"));
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.INVOCATION_FAILED, memberToString(method), this, "doh"));
    }

    @Test public void testShouldTraceNoComponent() throws IOException {
        monitor.noComponentFound(null, "doh");
        assertFileContent(getLogPrefix() + ComponentMonitorHelper.format(ComponentMonitorHelper.NO_COMPONENT, "doh"));
    }

    @Test public void shouldSerialize() throws IOException, ClassNotFoundException{
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutputStream oos = new ObjectOutputStream(bos);
    	oos.writeObject(monitor);
    	oos.close();

    	byte[] savedBytes = bos.toByteArray();
    	assertNotNull(savedBytes);
    	ByteArrayInputStream bis = new ByteArrayInputStream(savedBytes);
    	ObjectInputStream ois = new ObjectInputStream(bis);
    	this.monitor = (ComponentMonitor) ois.readObject();
    	assertNotNull(monitor);
    	//Run through a couple of other tests so we know that the delegate logger internally
    	//appears to be working.
    	testShouldTraceInstantiatedWithInjected();
    }


    protected void assertFileContent(final String line) throws IOException{
        List<String> lines = toLines(new StringReader(ForTestSakeAppender.CONTENT ));
        String s = lines.toString();
        assertTrue("Line '" + line + "' not found.  Instead got: " + line, s.indexOf(line) > 0);
    }

    protected List<String> toLines(final Reader resource) throws IOException {
        BufferedReader br = new BufferedReader(resource);
        List<String> lines = new ArrayList<String>();
        String line = br.readLine();
        while (line != null) {
            lines.add(line);
            line = br.readLine();
        }
        return lines;
    }
    
	@Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAllMethodsCallDelegatesAndAllMethodsCanHandleNullArgs() {
		monitor = makeComponentMonitorWithDelegate(delegate);
    	context.checking(new Expectations() {{
    		oneOf(delegate).changedBehavior(with(same((ChangedBehavior)null)));
    		oneOf(delegate).instantiated(null, null, null, null, null, 0);
    		oneOf(delegate).instantiating(null, null, null);
    		oneOf(delegate).instantiationFailed(null, null, null, null);
    		oneOf(delegate).invoked(null, null, null, null, 0, null);
    		oneOf(delegate).invoking(null, null, null, null);

    		oneOf(delegate).lifecycleInvocationFailed(null, null, null, null, null);
    		will(throwException(new PicoLifecycleException(null, null, null)));

    		oneOf(delegate).newInjector(null);
    		oneOf(delegate).noComponentFound(null, null);
    	}});
    	
    	monitor.changedBehavior(null);
    	monitor.instantiated(null, null, null, null, null, 0);
    	monitor.instantiating(null, null, null);
    	monitor.instantiationFailed(null, null, null, null);
    	monitor.invoked(null, null, null, null, 0, null);
    	monitor.invoking(null, null, null, null);
    	try {
			monitor.lifecycleInvocationFailed(null, null, null, null, null);
			fail("Should have thrown PicoLifecycleException");
		} catch (PicoLifecycleException e) {
			//a-ok
		}
    	monitor.newInjector(null);
    	monitor.noComponentFound(null, null);    	
    	
    }
    

}
