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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.AbstractButton;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.tck.AbstractComponentAdapterTest;
import org.picocontainer.testmodel.DependsOnTouchable;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;


@SuppressWarnings("serial")
public class ConstructorInjectorTestCase extends AbstractComponentAdapterTest {

	private final Mockery mockery = mockeryWithCountingNamingScheme();

    @Override
	protected Class getComponentAdapterType() {
        return ConstructorInjection.ConstructorInjector.class;
    }

    @Override
	protected ComponentAdapter prepDEF_verifyWithoutDependencyWorks(final MutablePicoContainer picoContainer) {
        return new ConstructorInjection.ConstructorInjector<A>("foo", A.class);
    }

    public static class A {
        public A() {
            fail("verification should not instantiate");
        }
    }

    public static class B {
        public B(final A a) {
            fail("verification should not instantiate");
        }
    }

    @Override
	protected ComponentAdapter prepDEF_verifyDoesNotInstantiate(final MutablePicoContainer picoContainer) {
        picoContainer.addComponent(A.class);
        return new ConstructorInjection.ConstructorInjector<B>(B.class, B.class);
    }

    @Override
	protected ComponentAdapter prepDEF_visitable() {
        return new ConstructorInjection.ConstructorInjector("bar", B.class, new ConstructorParameters(new Parameter[] {ComponentParameter.DEFAULT}));
    }

    @Override
	protected ComponentAdapter prepDEF_isAbleToTakeParameters(final MutablePicoContainer picoContainer) {
        picoContainer.addComponent(SimpleTouchable.class);
        return new ConstructorInjection.ConstructorInjector(
                NamedDependsOnTouchable.class, NamedDependsOnTouchable.class,
                new ConstructorParameters(new Parameter[] {ComponentParameter.DEFAULT, new ConstantParameter("Name")}));
    }

    @Override
	protected ComponentAdapter prepSER_isSerializable(final MutablePicoContainer picoContainer) {
        return new ConstructorInjection.ConstructorInjector(SimpleTouchable.class, SimpleTouchable.class);
    }

    @Override
	protected ComponentAdapter prepSER_isXStreamSerializable(final MutablePicoContainer picoContainer) {
        return prepSER_isSerializable(picoContainer);
    }

    public static class NamedDependsOnTouchable extends DependsOnTouchable {
        public NamedDependsOnTouchable(final Touchable t, final String name) {
            super(t);
        }
    }

    @Override
	protected ComponentAdapter prepVER_verificationFails(final MutablePicoContainer picoContainer) {
        return new ConstructorInjection.ConstructorInjector(DependsOnTouchable.class, DependsOnTouchable.class);
    }

    @Override
	protected ComponentAdapter prepINS_createsNewInstances(final MutablePicoContainer picoContainer) {
        return new ConstructorInjection.ConstructorInjector(SimpleTouchable.class, SimpleTouchable.class);
    }

    public static class Erroneous {
        public Erroneous() {
            throw new VerifyError("test");
        }
    }

    @Override
	protected ComponentAdapter prepINS_errorIsRethrown(final MutablePicoContainer picoContainer) {
        return new ConstructorInjection.ConstructorInjector(Erroneous.class, Erroneous.class);
    }

    public static class RuntimeThrowing {
        public RuntimeThrowing() {
            throw new RuntimeException("test");
        }
    }

    @Override
	protected ComponentAdapter prepINS_runtimeExceptionIsRethrown(final MutablePicoContainer picoContainer) {
        return new ConstructorInjection.ConstructorInjector(RuntimeThrowing.class, RuntimeThrowing.class);
    }

    public static class NormalExceptionThrowing {
        public NormalExceptionThrowing() throws Exception {
            throw new Exception("test");
        }
    }

    @Override
	protected ComponentAdapter prepINS_normalExceptionIsRethrownInsidePicoInitializationException(
            final MutablePicoContainer picoContainer) {
        return new ConstructorInjection.ConstructorInjector(NormalExceptionThrowing.class, NormalExceptionThrowing.class);
    }

    @Override
	protected ComponentAdapter prepRES_dependenciesAreResolved(final MutablePicoContainer picoContainer) {
        picoContainer.addComponent(SimpleTouchable.class);
        return new ConstructorInjection.ConstructorInjector(DependsOnTouchable.class, DependsOnTouchable.class);
    }

    public static class C1 {
        public C1(final C2 c2) {
            fail("verification should not instantiate");
        }
    }

    public static class C2 {
        public C2(final C1 c1) {
            fail("verification should not instantiate");
        }
    }

    @Override
	protected ComponentAdapter prepRES_failingVerificationWithCyclicDependencyException(final MutablePicoContainer picoContainer) {
        final ComponentAdapter<C1> componentAdapter = new ConstructorInjection.ConstructorInjector<C1>(C1.class, C1.class);
        picoContainer.addAdapter(componentAdapter);
        picoContainer.addComponent(C2.class, C2.class);
        return componentAdapter;
    }

    @Override
	protected ComponentAdapter prepRES_failingInstantiationWithCyclicDependencyException(final MutablePicoContainer picoContainer) {
        final ComponentAdapter<C1> componentAdapter = new ConstructorInjection.ConstructorInjector<C1>(C1.class, C1.class);
        picoContainer.addAdapter(componentAdapter);
        picoContainer.addComponent(C2.class, C2.class);
        return componentAdapter;
    }

    @Test public void testNormalExceptionThrownInCtorIsRethrownInsideInvocationTargetExeption() {
        DefaultPicoContainer picoContainer = new DefaultPicoContainer();
        picoContainer.addComponent(NormalExceptionThrowing.class);
        try {
            picoContainer.getComponent(NormalExceptionThrowing.class);
            fail();
        } catch (PicoCompositionException e) {
            assertEquals("test", e.getCause().getMessage());
        }
    }

    public static class InstantiationExceptionThrowing {
        public InstantiationExceptionThrowing() {
            throw new RuntimeException("Barf");
        }
    }

    @Test public void testInstantiationExceptionThrownInCtorIsRethrownInsideInvocationTargetExeption() {
        DefaultPicoContainer picoContainer = new DefaultPicoContainer();
        try {
            picoContainer.addComponent(InstantiationExceptionThrowing.class);
            picoContainer.getComponent(InstantiationExceptionThrowing.class);
            fail();
        } catch (RuntimeException e) {
            assertEquals("Barf", e.getMessage());
        }
    }

    public static class AllConstructorsArePrivate {
        private AllConstructorsArePrivate() {
        }
    }

    @Test public void testPicoInitializationExceptionThrownBecauseOfFilteredConstructors() {
        DefaultPicoContainer picoContainer = new DefaultPicoContainer(new ConstructorInjection());
        try {
            picoContainer.addComponent(AllConstructorsArePrivate.class);
            picoContainer.getComponent(AllConstructorsArePrivate.class);
            fail();
        } catch (PicoCompositionException e) {
            String s = e.getMessage();
            assertTrue(s.indexOf("constructors were not accessible") > 0);
            assertTrue(s.indexOf(AllConstructorsArePrivate.class.getName()) > 0);
        }
    }

    @Test public void testRegisterInterfaceShouldFail() throws PicoCompositionException {
        MutablePicoContainer pico = new DefaultPicoContainer();

        try {
            pico.addComponent(Runnable.class);
            fail("Shouldn't be allowed to register abstract classes or interfaces.");
        } catch (AbstractInjector.NotConcreteRegistrationException e) {
            assertEquals(Runnable.class, e.getComponentImplementation());
            assertTrue(e.getMessage().indexOf(Runnable.class.getName()) > 0);
        }
    }

    @Test public void testRegisterAbstractShouldFail() throws PicoCompositionException {
        MutablePicoContainer pico = new DefaultPicoContainer();

        try {
            pico.addComponent(AbstractButton.class);
            fail("Shouldn't be allowed to register abstract classes or interfaces.");
        } catch (AbstractInjector.NotConcreteRegistrationException e) {
            assertEquals(AbstractButton.class, e.getComponentImplementation());
            assertTrue(e.getMessage().indexOf(AbstractButton.class.getName()) > 0);
        }
    }

    private static class Private {
        private Private() {
        }
    }

    private static class NotYourBusiness {
        private NotYourBusiness(final Private aPrivate) {
            assertNotNull(aPrivate);
        }
    }

    static public class Component201 {
        public Component201(final String s) {
        }

        protected Component201(final Integer i, final Boolean b) {
            fail("Wrong constructor taken.");
        }
    }

    // http://jira.codehaus.org/browse/PICO-201
    @Test public void testShouldNotConsiderNonPublicConstructors() {
    	//Fix, default constructor gives adapting behavior, not straight constructorinjection.
        DefaultPicoContainer pico = new DefaultPicoContainer(new ConstructorInjection());
        pico.addComponent(Component201.class);
        pico.addComponent(new Integer(2));
        pico.addComponent(Boolean.TRUE);
        pico.addComponent("Hello");
        assertNotNull(pico.getComponent(Component201.class));
    }

    @Test public void testMonitoringHappensBeforeAndAfterInstantiation() throws NoSuchMethodException {
    	final ComponentMonitor monitor = mockery.mock(ComponentMonitor.class);
        final Constructor emptyHashMapCtor = HashMap.class.getConstructor();
        final Matcher<Long> durationIsGreaterThanOrEqualToZero = new BaseMatcher<Long>() {
        	public boolean matches(final Object item) {
                Long duration = (Long)item;
                return 0 <= duration;
			}

			public void describeTo(final Description description) {
                description.appendText("The endTime wasn't after the startTime");
			}
        };

        final Matcher<Object> isAHashMapThatWozCreated = new BaseMatcher<Object>() {
        	public boolean matches(final Object item) {
                return item instanceof HashMap;
            }

			public void describeTo(final Description description) {
                description.appendText("Should have been a hashmap");
			}
        };

        final Matcher<Object[]> injectedIsEmptyArray = new BaseMatcher<Object[]>() {
        	public boolean matches(final Object item) {
                Object[] injected = (Object[])item;
                return 0 == injected.length;
            }
        	public void describeTo(final Description description) {
                description.appendText("Should have had nothing injected into it");
            }
        };

        mockery.checking(new Expectations() {{
        	one(monitor).instantiating(with(any(PicoContainer.class)), (ComponentAdapter)with(a(ConstructorInjection.ConstructorInjector.class)), with(equal(emptyHashMapCtor)));
        	will(returnValue(emptyHashMapCtor));
        	one(monitor).instantiated(with(any(PicoContainer.class)), (ComponentAdapter)with(a(ConstructorInjection.ConstructorInjector.class)), with(equal(emptyHashMapCtor)),
        			with(isAHashMapThatWozCreated), with(injectedIsEmptyArray),
        			with(durationIsGreaterThanOrEqualToZero));
        }});

        ConstructorInjection.ConstructorInjector cica = new ConstructorInjection.ConstructorInjector(monitor, false,
                Map.class, HashMap.class, new ConstructorParameters(new Parameter[0]));
        cica.getComponentInstance(null, ComponentAdapter.NOTHING.class);
    }

    @Test public void testMonitoringHappensBeforeAndOnFailOfImpossibleComponentsInstantiation() throws NoSuchMethodException {
    	final ComponentMonitor monitor = mockery.mock(ComponentMonitor.class);
        final Constructor barfingActionListenerCtor = BarfingActionListener.class.getConstructor();

        final Matcher<Exception> isITE = new BaseMatcher<Exception>() {
        	public boolean matches(final Object item) {
        		 Exception ex = (Exception)item;
                 return ex instanceof InvocationTargetException;
            }

			public void describeTo(final Description description) {
                description.appendText("Should have been unable to instantiate");
			}
        };

        mockery.checking(new Expectations() {{
        	one(monitor).instantiating(with(any(PicoContainer.class)), (ComponentAdapter)with(a(ConstructorInjection.ConstructorInjector.class)), with(equal(barfingActionListenerCtor)));
        	will(returnValue(barfingActionListenerCtor));
        	one(monitor).instantiationFailed(with(any(PicoContainer.class)), (ComponentAdapter)with(a(ConstructorInjection.ConstructorInjector.class)), with(equal(barfingActionListenerCtor)),
        			with(isITE));
        }});


        ConstructorInjection.ConstructorInjector cica = new ConstructorInjection.ConstructorInjector(monitor, false,
                ActionListener.class, BarfingActionListener.class, new ConstructorParameters(new Parameter[0]));
        try {
            cica.getComponentInstance(null, ComponentAdapter.NOTHING.class);
            fail("Should barf");
        } catch (RuntimeException e) {
            assertEquals("Barf!", e.getMessage());
        }
    }

    private static class BarfingActionListener implements ActionListener {
        public BarfingActionListener() {
            throw new RuntimeException("Barf!");
        }

        public void actionPerformed(final ActionEvent e) {
        }
    }

    public static class One {
        public One(final Two two) {
            two.inc();
        }
    }
    public static class Two {
        private int inc;
        public void inc() {
            inc++;
        }

        public long howMany() {
            return inc;
        }
    }

    /*
     * (TODO:  On some machines, the number of iterations aren't enough.)
     */
    @Test public void testSpeedOfRememberedConstructor()  {
        long with, without;

        ConstructorInjection injectionType = new ForgetfulConstructorInjection();
        timeIt(injectionType, 10); // discard
        timeIt(injectionType, 10); // discard
        timeIt(injectionType, 10); // discard
        without = timeIt(injectionType, 20000);
        injectionType = new ConstructorInjection();
        garbageCollect();
        timeIt(injectionType, 10); // discard
        timeIt(injectionType, 10); // discard
        timeIt(injectionType, 10); // discard
        with = timeIt(injectionType, 20000);
        assertTrue("'with' should be less than 'without' but they were in fact with: " + with + ", and without:" + without, with < without);
    }

	private void garbageCollect() throws Error {
		Runtime rt = Runtime.getRuntime();
        rt.gc();
        rt.runFinalization();
        final CountDownLatch latch = new CountDownLatch(1);
        new Object() {
          @Override
		protected void finalize() {
            latch.countDown();
          }
        };
        rt.gc();
        rt.runFinalization();
        try {
          latch.await();
        }
        catch(InterruptedException ie){
          throw new Error(ie);
        }
	}

    private long timeIt(final ConstructorInjection injectionType, final int iterations) {
        DefaultPicoContainer dpc = new DefaultPicoContainer(injectionType);
        Two two = new Two();
        dpc.addComponent(two);
        dpc.addComponent(One.class);
        long start = System.currentTimeMillis();
        for (int x = 0; x < iterations; x++) {
                dpc.getComponent(One.class);
            }
        long end = System.currentTimeMillis();
        assertEquals(iterations, two.howMany());
        return end-start;
    }

}
