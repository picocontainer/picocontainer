/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
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
import org.picocontainer.InjectionType;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.tck.AbstractComponentAdapterTest;
import org.picocontainer.testmodel.DependsOnTouchable;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;


@SuppressWarnings("serial")
public class ConstructorInjectorTestCase extends AbstractComponentAdapterTest {

	private Mockery mockery = mockeryWithCountingNamingScheme();

    protected Class getComponentAdapterType() {
        return ConstructorInjector.class;
    }

    protected ComponentAdapter prepDEF_verifyWithoutDependencyWorks(MutablePicoContainer picoContainer) {
        return new ConstructorInjector("foo", A.class, null, new NullComponentMonitor(), false);
    }

    public static class A {
        public A() {
            fail("verification should not instantiate");
        }
    }

    public static class B {
        public B(A a) {
            fail("verification should not instantiate");
        }
    }

    protected ComponentAdapter prepDEF_verifyDoesNotInstantiate(MutablePicoContainer picoContainer) {
        picoContainer.addComponent(A.class);
        return new ConstructorInjector(B.class, B.class, null, new NullComponentMonitor(), false);
    }

    protected ComponentAdapter prepDEF_visitable() {
        return new ConstructorInjector("bar", B.class, new Parameter[] {ComponentParameter.DEFAULT} , new NullComponentMonitor(), false);
    }

    protected ComponentAdapter prepDEF_isAbleToTakeParameters(MutablePicoContainer picoContainer) {
        picoContainer.addComponent(SimpleTouchable.class);
        return new ConstructorInjector(
                NamedDependsOnTouchable.class, NamedDependsOnTouchable.class,
                new Parameter[] {ComponentParameter.DEFAULT, new ConstantParameter("Name")} , new NullComponentMonitor(), false);
    }

    protected ComponentAdapter prepSER_isSerializable(MutablePicoContainer picoContainer) {
        return new ConstructorInjector(SimpleTouchable.class, SimpleTouchable.class, null, new NullComponentMonitor(), false);
    }

    protected ComponentAdapter prepSER_isXStreamSerializable(final MutablePicoContainer picoContainer) {
        return prepSER_isSerializable(picoContainer);
    }

    public static class NamedDependsOnTouchable extends DependsOnTouchable {
        public NamedDependsOnTouchable(Touchable t, String name) {
            super(t);
        }
    }

    protected ComponentAdapter prepVER_verificationFails(MutablePicoContainer picoContainer) {
        return new ConstructorInjector(DependsOnTouchable.class, DependsOnTouchable.class, null, new NullComponentMonitor(), false);
    }

    protected ComponentAdapter prepINS_createsNewInstances(MutablePicoContainer picoContainer) {
        return new ConstructorInjector(SimpleTouchable.class, SimpleTouchable.class, null, new NullComponentMonitor(), false);
    }

    public static class Erroneous {
        public Erroneous() {
            throw new VerifyError("test");
        }
    }

    protected ComponentAdapter prepINS_errorIsRethrown(MutablePicoContainer picoContainer) {
        return new ConstructorInjector(Erroneous.class, Erroneous.class, null, new NullComponentMonitor(), false);
    }

    public static class RuntimeThrowing {
        public RuntimeThrowing() {
            throw new RuntimeException("test");
        }
    }

    protected ComponentAdapter prepINS_runtimeExceptionIsRethrown(MutablePicoContainer picoContainer) {
        return new ConstructorInjector(RuntimeThrowing.class, RuntimeThrowing.class, null, new NullComponentMonitor(), false);
    }

    public static class NormalExceptionThrowing {
        public NormalExceptionThrowing() throws Exception {
            throw new Exception("test");
        }
    }

    protected ComponentAdapter prepINS_normalExceptionIsRethrownInsidePicoInitializationException(
            MutablePicoContainer picoContainer) {
        return new ConstructorInjector(NormalExceptionThrowing.class, NormalExceptionThrowing.class, null, new NullComponentMonitor(), false);
    }

    protected ComponentAdapter prepRES_dependenciesAreResolved(MutablePicoContainer picoContainer) {
        picoContainer.addComponent(SimpleTouchable.class);
        return new ConstructorInjector(DependsOnTouchable.class, DependsOnTouchable.class, null, new NullComponentMonitor(), false);
    }

    public static class C1 {
        public C1(C2 c2) {
            fail("verification should not instantiate");
        }
    }

    public static class C2 {
        public C2(C1 c1) {
            fail("verification should not instantiate");
        }
    }

    protected ComponentAdapter prepRES_failingVerificationWithCyclicDependencyException(MutablePicoContainer picoContainer) {
        final ComponentAdapter componentAdapter = new ConstructorInjector(C1.class, C1.class, null, new NullComponentMonitor(), false);
        picoContainer.addAdapter(componentAdapter);
        picoContainer.addComponent(C2.class, C2.class);
        return componentAdapter;
    }

    protected ComponentAdapter prepRES_failingInstantiationWithCyclicDependencyException(MutablePicoContainer picoContainer) {
        final ComponentAdapter componentAdapter = new ConstructorInjector(C1.class, C1.class, null, new NullComponentMonitor(), false);
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
        DefaultPicoContainer picoContainer = new DefaultPicoContainer();
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
        private NotYourBusiness(Private aPrivate) {
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
        DefaultPicoContainer pico = new DefaultPicoContainer();
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
        	public boolean matches(Object item) {
                Long duration = (Long)item;
                return 0 <= duration;
			}

			public void describeTo(Description description) {
                description.appendText("The endTime wasn't after the startTime");				
			}
        };
        
        final Matcher<Object> isAHashMapThatWozCreated = new BaseMatcher<Object>() {
        	public boolean matches(Object item) {
                return item instanceof HashMap;
            }

			public void describeTo(Description description) {
                description.appendText("Should have been a hashmap");				
			}
        };

        final Matcher<Object[]> injectedIsEmptyArray = new BaseMatcher<Object[]>() {
        	public boolean matches(Object item) {
                Object[] injected = (Object[])item;
                return 0 == injected.length;
            }
        	public void describeTo(Description description) {
                description.appendText("Should have had nothing injected into it");
            }
        };

        mockery.checking(new Expectations(){{
        	one(monitor).instantiating(with(any(PicoContainer.class)), (ComponentAdapter)with(a(ConstructorInjector.class)), with(equal(emptyHashMapCtor)));
        	will(returnValue(emptyHashMapCtor));
        	one(monitor).instantiated(with(any(PicoContainer.class)), (ComponentAdapter)with(a(ConstructorInjector.class)), with(equal(emptyHashMapCtor)), 
        			with(isAHashMapThatWozCreated), with(injectedIsEmptyArray), 
        			with(durationIsGreaterThanOrEqualToZero));
        }});

        ConstructorInjector cica = new ConstructorInjector(
                Map.class, HashMap.class, new Parameter[0], monitor, false);
        cica.getComponentInstance(null, ComponentAdapter.NOTHING.class);
    }

    @Test public void testMonitoringHappensBeforeAndOnFailOfImpossibleComponentsInstantiation() throws NoSuchMethodException {
    	final ComponentMonitor monitor = mockery.mock(ComponentMonitor.class);
        final Constructor barfingActionListenerCtor = BarfingActionListener.class.getConstructor();

        final Matcher<Exception> isITE = new BaseMatcher<Exception>() {
        	public boolean matches(Object item) {
        		 Exception ex = (Exception)item;
                 return ex instanceof InvocationTargetException;
            }

			public void describeTo(Description description) {
                description.appendText("Should have been unable to instantiate");				
			}
        };

        mockery.checking(new Expectations(){{
        	one(monitor).instantiating(with(any(PicoContainer.class)), (ComponentAdapter)with(a(ConstructorInjector.class)), with(equal(barfingActionListenerCtor)));
        	will(returnValue(barfingActionListenerCtor));
        	one(monitor).instantiationFailed(with(any(PicoContainer.class)), (ComponentAdapter)with(a(ConstructorInjector.class)), with(equal(barfingActionListenerCtor)),
        			with(isITE));
        }});


        ConstructorInjector cica = new ConstructorInjector(
                ActionListener.class, BarfingActionListener.class, new Parameter[0], monitor, false);
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

        public void actionPerformed(ActionEvent e) {
        }
    }

    public static class One {
        public One(Two two) {
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
        injectionType = new ForgetfulConstructorInjection();
        timeIt(); // discard
        timeIt(); // discard
        timeIt(); // discard
        without = timeIt();
        injectionType = new ConstructorInjection();
        with = timeIt();
        assertTrue("'with' should be less than 'without' but they were in fact: " + with + ", and " + without, with < without);
    }

    InjectionType injectionType;
    private long timeIt() {
        int iterations = 20000;
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
