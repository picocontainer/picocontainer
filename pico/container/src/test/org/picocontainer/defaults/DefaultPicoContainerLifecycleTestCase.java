/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the license.txt file.                                                    *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.defaults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoLifecycleException;
import org.picocontainer.Startable;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.monitors.LifecycleComponentMonitor;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.monitors.LifecycleComponentMonitor.LifecycleFailuresException;
import org.picocontainer.testmodel.RecordingLifecycle.FiveTriesToBeMalicious;
import org.picocontainer.testmodel.RecordingLifecycle.Four;
import org.picocontainer.testmodel.RecordingLifecycle.One;
import org.picocontainer.testmodel.RecordingLifecycle.Three;
import org.picocontainer.testmodel.RecordingLifecycle.Two;

/**
 * This class tests the lifecycle aspects of DefaultPicoContainer.
 *
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 * @author Ward Cunningham
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class DefaultPicoContainerLifecycleTestCase {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    @Test public void testOrderOfInstantiationShouldBeDependencyOrder() throws Exception {

        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent("recording", StringBuffer.class);
        pico.addComponent(Four.class);
        pico.addComponent(Two.class);
        pico.addComponent(One.class);
        pico.addComponent(Three.class);
        final List componentInstances = pico.getComponents();

        // instantiation - would be difficult to do these in the wrong order!!
        assertEquals("Incorrect Order of Instantiation", One.class, componentInstances.get(1).getClass());
        assertEquals("Incorrect Order of Instantiation", Two.class, componentInstances.get(2).getClass());
        assertEquals("Incorrect Order of Instantiation", Three.class, componentInstances.get(3).getClass());
        assertEquals("Incorrect Order of Instantiation", Four.class, componentInstances.get(4).getClass());
    }

    @Test public void testOrderOfStartShouldBeDependencyOrderAndStopAndDisposeTheOpposite() throws Exception {
        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        MutablePicoContainer child = parent.makeChildContainer();

        parent.addComponent("recording", StringBuffer.class);
        child.addComponent(Four.class);
        parent.addComponent(Two.class);
        parent.addComponent(One.class);
        child.addComponent(Three.class);

        parent.start();
        parent.stop();
        parent.dispose();

        assertEquals("<One<Two<Three<FourFour>Three>Two>One>!Four!Three!Two!One",
                parent.getComponent("recording").toString());
    }


    @Test public void testLifecycleIsIgnoredIfAdaptersAreNotLifecycleManagers() {
        DefaultPicoContainer parent = new DefaultPicoContainer(new ConstructorInjection());
        MutablePicoContainer child = parent.makeChildContainer();

        parent.addComponent("recording", StringBuffer.class);
        child.addComponent(Four.class);
        parent.addComponent(Two.class);
        parent.addComponent(One.class);
        child.addComponent(Three.class);

        parent.start();
        parent.stop();
        parent.dispose();

        assertEquals("",
                parent.getComponent("recording").toString());
    }

    @Test public void testStartStartShouldFail() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.start();
        try {
            pico.start();
            fail("Should have failed");
        } catch (IllegalStateException e) {
            // expected;
        }
    }

    @Test public void testStartStopStopShouldFail() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.start();
        pico.stop();
        try {
            pico.stop();
            fail("Should have failed");
        } catch (IllegalStateException e) {
            // expected;
        }
    }

    @Test public void testStartStopDisposeDisposeShouldFail() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.start();
        pico.stop();
        pico.dispose();
        try {
            pico.dispose();
            fail("Should have barfed");
        } catch (IllegalStateException e) {
            // expected;
        }
    }

    public static class FooRunnable implements Runnable, Startable {
        private int runCount;
        private Thread thread = new Thread();
        private boolean interrupted;

        public FooRunnable() {
        }

        public int runCount() {
            return runCount;
        }

        public boolean isInterrupted() {
            return interrupted;
        }

        public void start() {
            thread = new Thread(this);
            thread.start();
        }

        public void stop() {
            thread.interrupt();
        }

        // this would do something a bit more concrete
        // than counting in real life !
        public void run() {
            runCount++;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
    }

    @Test public void testStartStopOfDaemonizedThread() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer(new Caching());
        pico.addComponent(FooRunnable.class);

        pico.getComponents();
        pico.start();
        Thread.sleep(100);
        pico.stop();

        FooRunnable foo = pico.getComponent(FooRunnable.class);
        assertEquals(1, foo.runCount());
        pico.start();
        Thread.sleep(100);
        pico.stop();
        assertEquals(2, foo.runCount());
    }

    @Test public void testGetComponentInstancesOnParentContainerHostedChildContainerDoesntReturnParentAdapter() {
        MutablePicoContainer parent = new DefaultPicoContainer();
        MutablePicoContainer child = parent.makeChildContainer();
        assertEquals(0, child.getComponents().size());
    }

    @Test public void testComponentsAreStartedBreadthFirstAndStoppedAndDisposedDepthFirst() {
        MutablePicoContainer parent = new DefaultPicoContainer(new Caching());
        parent.addComponent(Two.class);
        parent.addComponent("recording", StringBuffer.class);
        parent.addComponent(One.class);
        MutablePicoContainer child = parent.makeChildContainer();
        child.addComponent(Three.class);
        parent.start();
        parent.stop();
        parent.dispose();

        assertEquals("<One<Two<ThreeThree>Two>One>!Three!Two!One", parent.getComponent("recording").toString());
    }

    @Test public void testMaliciousComponentCannotExistInAChildContainerAndSeeAnyElementOfContainerHierarchy() {
        MutablePicoContainer parent = new DefaultPicoContainer(new Caching());
        parent.addComponent(Two.class);
        parent.addComponent("recording", StringBuffer.class);
        parent.addComponent(One.class);
        parent.addComponent(Three.class);
        MutablePicoContainer child = parent.makeChildContainer();
        child.addComponent(FiveTriesToBeMalicious.class);
        try {
            parent.start();
            fail("Thrown " + AbstractInjector.UnsatisfiableDependenciesException.class.getName() + " expected");
        } catch ( AbstractInjector.UnsatisfiableDependenciesException e) {
            // FiveTriesToBeMalicious can't get instantiated as there is no PicoContainer in any component set
        }
        String recording = parent.getComponent("recording").toString();
        assertEquals("<One<Two<Three", recording);
        try {
            child.getComponent(FiveTriesToBeMalicious.class);
            fail("Thrown " + AbstractInjector.UnsatisfiableDependenciesException.class.getName() + " expected");
        } catch (final AbstractInjector.UnsatisfiableDependenciesException e) {
            // can't get instantiated as there is no PicoContainer in any component set
        }
        recording = parent.getComponent("recording").toString();
        assertEquals("<One<Two<Three", recording); // still the same
    }


    public static class NotStartable {
         public void start(){
            Assert.fail("start() should not get invoked on NonStartable");
        }
    }

    @Test public void testOnlyStartableComponentsAreStartedOnStart() {
        MutablePicoContainer pico = new DefaultPicoContainer(new Caching());
        pico.addComponent("recording", StringBuffer.class);
        pico.addComponent(One.class);
        pico.addComponent(NotStartable.class);
        pico.start();
        pico.stop();
        pico.dispose();
        assertEquals("<OneOne>!One", pico.getComponent("recording").toString());
    }

    @Test public void testShouldFailOnStartAfterDispose() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.dispose();
        try {
            pico.start();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test public void testShouldFailOnStopAfterDispose() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.dispose();
        try {
            pico.stop();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test public void testShouldStackContainersLast() {
        // this is merely a code coverage test - but it doesn't seem to cover the StackContainersAtEndComparator
        // fully. oh well.
        MutablePicoContainer pico = new DefaultPicoContainer(new Caching());
        pico.addComponent(ArrayList.class);
        pico.addComponent(DefaultPicoContainer.class);
        pico.addComponent(HashMap.class);
        pico.start();
        DefaultPicoContainer childContainer = pico.getComponent(DefaultPicoContainer.class);
        // it should be started too
        try {
            childContainer.start();
            fail();
        } catch (IllegalStateException e) {
        }
    }

    @Test public void testCanSpecifyLifeCycleStrategyForInstanceRegistrationWhenSpecifyingComponentFactory()
        throws Exception {
        LifecycleStrategy strategy = new LifecycleStrategy() {
            public void start(Object component) {
                ((StringBuffer)component).append("start>");
            }

            public void stop(Object component) {
                ((StringBuffer)component).append("stop>");
            }

            public void dispose(Object component) {
                ((StringBuffer)component).append("dispose>");
            }

            public boolean hasLifecycle(Class type) {
                return true;
            }

            public boolean isLazy(ComponentAdapter<?> adapter) {
                return false;
            }
        };
        MutablePicoContainer pico = new DefaultPicoContainer( new AdaptingInjection(), strategy, null );

        StringBuffer sb = new StringBuffer();

        pico.addComponent(sb);

        pico.start();
        pico.stop();
        pico.dispose();

        assertEquals("start>stop>dispose>", sb.toString());
    }

    @Test public void testLifeCycleStrategyForInstanceRegistrationPassedToChildContainers()
        throws Exception
    {
        LifecycleStrategy strategy = new LifecycleStrategy() {
            public void start(Object component) {
                ((StringBuffer)component).append("start>");
            }

            public void stop(Object component) {
                ((StringBuffer)component).append("stop>");
            }

            public void dispose(Object component) {
                ((StringBuffer)component).append("dispose>");
            }

            public boolean hasLifecycle(Class type) {
                return true;
            }

            public boolean isLazy(ComponentAdapter<?> adapter) {
                return false;
            }
        };
        MutablePicoContainer parent = new DefaultPicoContainer(strategy, null);
        MutablePicoContainer pico = parent.makeChildContainer();

        StringBuffer sb = new StringBuffer();

        pico.addComponent(sb);

        pico.start();
        pico.stop();
        pico.dispose();

        assertEquals("start>stop>dispose>", sb.toString());
    }


    @Test public void testLifecycleDoesNotRecoverWithNullComponentMonitor() {

    	final Startable s1 = mockery.mock(Startable.class, "s1");
        Startable s2 = mockery.mock(Startable.class, "s2");
        mockery.checking(new Expectations(){{
            one(s1).start();
            will(throwException(new RuntimeException("I do not want to start myself")));
        }});
 
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        dpc.addComponent("foo", s1);
        dpc.addComponent("bar", s2);
        try {
            dpc.start();
            fail("PicoLifecylceException expected");
        } catch (PicoLifecycleException e) {
            assertEquals("I do not want to start myself", e.getCause().getMessage());
        }
        dpc.stop();
    }

    @Test public void testLifecycleCanRecoverWithCustomComponentMonitor() throws NoSuchMethodException {

    	final Startable s1 = mockery.mock(Startable.class, "s1");
        final Startable s2 = mockery.mock(Startable.class, "s2");
        final ComponentMonitor cm = mockery.mock(ComponentMonitor.class);
    	mockery.checking(new Expectations(){{
            one(s1).start();
            will(throwException(new RuntimeException("I do not want to start myself")));
            one(s1).stop();
            one(s2).start();
            one(s2).stop();
            // s1 expectations
            one(cm).invoking(with(aNull(PicoContainer.class)), with(aNull(ComponentAdapter.class)), with(equal(Startable.class.getMethod("start", (Class[])null))), with(same(s1)), with(any(Object[].class)));
            one(cm).lifecycleInvocationFailed(with(aNull(MutablePicoContainer.class)), with(aNull(ComponentAdapter.class)), with(any(Method.class)), with(same(s1)), with(any(RuntimeException.class)));
            one(cm).invoking(with(aNull(PicoContainer.class)),
                    with(aNull(ComponentAdapter.class)),
                    with(equal(Startable.class.getMethod("stop", (Class[])null))),
                    with(same(s1)), with(any(Object[].class)));
            one(cm).invoked(with(aNull(PicoContainer.class)),
                    with(aNull(ComponentAdapter.class)),
                    with(equal(Startable.class.getMethod("stop", (Class[])null))),
                    with(same(s1)), with(any(Long.class)), with(any(Object[].class)), with(same(null)));
            // s2 expectations
            one(cm).invoking(with(aNull(PicoContainer.class)), with(aNull(ComponentAdapter.class)), with(equal(Startable.class.getMethod("start", (Class[])null))), with(same(s2)), with(any(Object[].class)));
            one(cm).invoked(with(aNull(PicoContainer.class)), with(aNull(ComponentAdapter.class)), with(equal(Startable.class.getMethod("start", (Class[])null))), with(same(s2)), with(any(Long.class)), with(any(Object[].class)), with(same(null)));
            one(cm).invoking(with(aNull(PicoContainer.class)), with(aNull(ComponentAdapter.class)), with(equal(Startable.class.getMethod("stop", (Class[])null))), with(same(s2)), with(any(Object[].class)));
            one(cm).invoked(with(aNull(PicoContainer.class)), with(aNull(ComponentAdapter.class)), with(equal(Startable.class.getMethod("stop", (Class[])null))), with(same(s2)), with(any(Long.class)), with(any(Object[].class)), with(same(null)));
    	}});

        DefaultPicoContainer dpc = new DefaultPicoContainer(cm);
        dpc.addComponent("foo", s1);
        dpc.addComponent("bar", s2);
        dpc.start();
        dpc.stop();
    }

    @Test public void testLifecycleFailuresCanBePickedUpAfterTheEvent() {
    	final Startable s1 = mockery.mock(Startable.class, "s1");
        final Startable s2 = mockery.mock(Startable.class, "s2");
        final Startable s3 = mockery.mock(Startable.class, "s3");
        mockery.checking(new Expectations(){{
            one(s1).start();
            will(throwException(new RuntimeException("I do not want to start myself")));
            one(s1).stop();
            one(s2).start();
            one(s2).stop();
            one(s3).start();
            will(throwException(new RuntimeException("I also do not want to start myself")));
            one(s3).stop();
        }});
        
        LifecycleComponentMonitor lifecycleComponentMonitor = new LifecycleComponentMonitor(new NullComponentMonitor());

        DefaultPicoContainer dpc = new DefaultPicoContainer(lifecycleComponentMonitor);
        dpc.addComponent("one", s1);
        dpc.addComponent("two", s2);
        dpc.addComponent("three", s3);

        dpc.start();

        try {
            lifecycleComponentMonitor.rethrowLifecycleFailuresException();
            fail("LifecycleFailuresException expected");
        } catch (LifecycleFailuresException e) {
            assertEquals("I do not want to start myself;  I also do not want to start myself;", e.getMessage().trim());
            dpc.stop();
            assertEquals(2, e.getFailures().size());
        }

    }

    @Test public void testStartedComponentsCanBeStoppedIfSomeComponentsFailToStart() {

    	final Startable s1 = mockery.mock(Startable.class, "s1");
        final Startable s2 = mockery.mock(Startable.class, "s2");
        mockery.checking(new Expectations(){{
            one(s1).start();
            one(s1).stop();
            one(s2).start();
            will(throwException(new RuntimeException("I do not want to start myself")));
         // s2 does not expect stop().
        }});
        
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        dpc.addComponent("foo", s1);
        dpc.addComponent("bar", s2);

        try {
            dpc.start();
            fail("PicoLifecylceException expected");
        } catch (RuntimeException e) {
            dpc.stop();
        }

    }

    @Test public void testStartedComponentsCanBeStoppedIfSomeComponentsFailToStartEvenInAPicoHierarchy() {

    	final Startable s1 = mockery.mock(Startable.class, "s1");
        final Startable s2 = mockery.mock(Startable.class, "s2");
        mockery.checking(new Expectations(){{
            one(s1).start();
            one(s1).stop();
            one(s2).start();
            will(throwException(new RuntimeException("I do not want to start myself")));
         // s2 does not expect stop().
        }});
        
        DefaultPicoContainer dpc = new DefaultPicoContainer();
        dpc.addComponent("foo", s1);
        dpc.addComponent("bar", s2);
        dpc.addChildContainer(new DefaultPicoContainer(dpc));

        try {
            dpc.start();
            fail("PicoLifecylceException expected");
        } catch (RuntimeException e) {
            dpc.stop();
        }

    }

    @Test public void testChildContainerIsStoppedWhenStartedIndependentlyOfParent() throws Exception {

        DefaultPicoContainer parent = new DefaultPicoContainer();

        parent.start();

        MutablePicoContainer child = parent.makeChildContainer();

        final Startable s1 = mockery.mock(Startable.class, "s1");
        mockery.checking(new Expectations(){{
            one(s1).start();
            one(s1).stop();
        }});
        
        child.addComponent(s1);

        child.start();
        parent.stop();

    }
}