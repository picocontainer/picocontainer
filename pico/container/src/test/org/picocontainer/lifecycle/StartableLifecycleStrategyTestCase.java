/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.picocontainer.Characteristics.CACHE;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.io.Serializable;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.Disposable;
import org.picocontainer.PicoLifecycleException;
import org.picocontainer.Startable;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;

/**
 * 
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
@RunWith(JMock.class)
public class StartableLifecycleStrategyTestCase {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    private StartableLifecycleStrategy startableLifecycle;

    @Before
    public void setUp(){
        startableLifecycle = new StartableLifecycleStrategy(new NullComponentMonitor());
    }

    @Test public void testStartable(){
        Object startable = mockComponent(true, false);
        startableLifecycle.start(startable);
        startableLifecycle.stop(startable);
    }

    @Test public void testDisposable(){
        Object startable = mockComponent(false, true);
        startableLifecycle.dispose(startable);
    }

    @Test public void testSerializable(){
        Object serializable = mockComponent(false, false);
        startableLifecycle.start(serializable);
        startableLifecycle.stop(serializable);
        startableLifecycle.dispose(serializable);
    }
    
    private Object mockComponent(boolean startable, boolean disposeable) {
        if ( startable ) {
        	 final Startable mock = mockery.mock(Startable.class);
        	 mockery.checking(new Expectations() {{
                 one(mock).start(); 
                 one(mock).stop(); 
             }});
        	 return mock;
        }
        if ( disposeable ) {
       	 final Disposable mock = mockery.mock(Disposable.class);
    	 mockery.checking(new Expectations() {{
             one(mock).dispose(); 
         }});
    	 return mock;
        }
        return mockery.mock(Serializable.class);
    }



    public static class ThirdPartyStartableComponent2 implements ThirdPartyStartable {
        public void sstart() {
            throw new UnsupportedOperationException();
        }
        public void sstop() {
        }

        public void ddispose() {
        }
    }

    public static class ThirdPartyStartableComponent3 implements ThirdPartyStartable {
        public void sstart() throws Exception {
            throw new Exception("whoaa!");
        }
        public void sstop() {
        }

        public void ddispose() {
        }
    }

    @Test public void testThirdPartyStartableAndDisposable() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new MyStartableLifecycleStrategy());
        StringBuilder sb = new StringBuilder();
        pico.addComponent(sb);
        pico.as(CACHE).addComponent(ThirdPartyStartableComponent.class);
        pico.start();
        pico.stop();
        pico.dispose();
        assertEquals("<>!", sb.toString());

    }

    @Test public void testLateAdditionofStartableAlsoStarted() {
    	DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new MyStartableLifecycleStrategy());
    	StringBuilder sb = new StringBuilder();
    	pico.addComponent(sb);
    	pico.as(CACHE).addComponent(NonStartable.class);
    	pico.start();
    	assertEquals("", sb.toString());
        pico.as(CACHE).addComponent(ThirdPartyStartableComponent.class);
    	assertEquals("<", sb.toString());
    	pico.stop();
    	pico.dispose();
    	assertEquals("<>!", sb.toString());
    }

    public static class NonStartable {

    }

    @Test public void testMixOfThirdPartyAndBuiltInStartableAndDisposable() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new CompositeLifecycleStrategy(
                    new MyStartableLifecycleStrategy(),
                    new StartableLifecycleStrategy(new NullComponentMonitor()))
        );
        StringBuilder sb = new StringBuilder();
        pico.addComponent(sb);
        pico.as(CACHE).addComponent(ThirdPartyStartableComponent.class);
        pico.as(CACHE).addComponent(BuiltInStartableComponent.class);
        pico.start();
        pico.stop();
        pico.dispose();
        assertEquals("<<>>!!", sb.toString());
    }

    @Test public void testThirdPartyStartableCanNoteLifecycleRuntimeException() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new MyStartableLifecycleStrategy());
        pico.as(CACHE).addComponent(ThirdPartyStartableComponent2.class);
        try {
            pico.start();
            fail("should have barfed");
        } catch (PicoLifecycleException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
            assertTrue(e.getInstance() instanceof ThirdPartyStartableComponent2);
            assertEquals("sstart", e.getMethod().getName());
            // expected
        }

    }

    @Test public void testThirdPartyStartableCanNoteLifecycleException() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new EmptyPicoContainer(), new MyStartableLifecycleStrategy());
        pico.as(CACHE).addComponent(ThirdPartyStartableComponent3.class);
        try {
            pico.start();
            fail("should have barfed");
        } catch (PicoLifecycleException e) {
            Throwable throwable = e.getCause();
            assertTrue(throwable instanceof Exception);
            String s = throwable.getMessage();
            assertEquals("whoaa!", s);
            assertTrue(e.getInstance() instanceof ThirdPartyStartableComponent3);
            assertEquals("sstart", e.getMethod().getName());
            // expected
        }

    }

}
