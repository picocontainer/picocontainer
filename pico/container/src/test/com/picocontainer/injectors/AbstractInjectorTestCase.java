/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.injectors;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.containers.EmptyPicoContainer;
import com.picocontainer.injectors.AbstractInjector;
import com.picocontainer.injectors.AbstractInjector.AmbiguousComponentResolutionException;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.parameters.ConstructorParameters;

@SuppressWarnings("serial")
public class AbstractInjectorTestCase {

    private AbstractInjector ai;
    Constructor<HashMap> ctor;

    @Before
    public void setUp() throws NoSuchMethodException {
        ai = new MyAbstractInjector(Map.class, HashMap.class, new Parameter[0], new NullComponentMonitor(), false);
        ctor = HashMap.class.getConstructor();
    }

    @Test public void testCaughtIllegalAccessExceptionInvokesMonitorAndThrows() {
        final EmptyPicoContainer epc = new EmptyPicoContainer();
        final IllegalAccessException iae = new IllegalAccessException("foo");
        NullComponentMonitor ncm = new NullComponentMonitor() {
            @Override
			public void instantiationFailed(final PicoContainer container,
                                            final ComponentAdapter componentAdapter,
                                            final Constructor constructor,
                                            final Exception e) {
                assertSame(epc, container);
                assertSame(ai, componentAdapter);
                assertSame(ctor, constructor);
                assertSame(iae, e);
            }
        };
        try {
            ai.caughtIllegalAccessException(ncm, ctor, iae, epc);
        } catch (PicoCompositionException e) {
            assertSame(iae, e.getCause());
        }
    }

    @Test public void testCaughtInstantiationExceptionInvokesMonitorAndThrows() {
        final EmptyPicoContainer epc = new EmptyPicoContainer();
        final InstantiationException ie = new InstantiationException("foo");
        NullComponentMonitor ncm = new NullComponentMonitor() {
            @Override
			public void instantiationFailed(final PicoContainer container,
                                            final ComponentAdapter componentAdapter,
                                            final Constructor constructor,
                                            final Exception e) {
                assertSame(epc, container);
                assertSame(ai, componentAdapter);
                assertSame(ctor, constructor);
                assertSame(ie, e);
            }
        };
        try {
            ai.caughtInstantiationException(ncm, ctor, ie, epc);
        } catch (PicoCompositionException e) {
            assertSame("Should never get here", e.getMessage());
        }
    }

    @Test public void testCaughtInvocationTargetExceptionInvokesMonitorAndReThrowsRuntimeIfRuntimeInTheFirstPlace() {
        final InvocationTargetException ite = new InvocationTargetException(new RuntimeException("foo"));
        NullComponentMonitor ncm = new NullComponentMonitor() {
            @Override
			public void invocationFailed(final Member member, final Object instance, final Exception e) {
                assertSame(ctor, member);
                assertSame("bar", instance);
                assertSame(ite, e);
            }
        };
        try {
            ai.caughtInvocationTargetException(ncm, ctor, "bar", ite);
        } catch (RuntimeException e) {
            assertSame("foo", e.getMessage());
        }
    }

    @Test public void testCaughtInvocationTargetExceptionInvokesMonitorAndReThrowsErrorIfErrorInTheFirstPlace() {
        final InvocationTargetException ite = new InvocationTargetException(new Error("foo"));
        NullComponentMonitor ncm = new NullComponentMonitor() {
            @Override
			public void invocationFailed(final Member member, final Object instance, final Exception e) {
                assertSame(ctor, member);
                assertSame("bar", instance);
                assertSame(ite, e);
            }
        };
        try {
            ai.caughtInvocationTargetException(ncm, ctor, "bar", ite);
        } catch (Error e) {
            assertSame("foo", e.getMessage());
        }
    }

    @Test public void testCaughtInvocationTargetExceptionInvokesMonitorAndReThrowsAsCompositionIfNotRuntimeOrError() {
        final InvocationTargetException ite = new InvocationTargetException(new Exception("foo"));
        NullComponentMonitor ncm = new NullComponentMonitor() {
            @Override
			public void invocationFailed(final Member member, final Object instance, final Exception e) {
                assertSame(ctor, member);
                assertSame("bar", instance);
                assertSame(ite, e);
            }
        };
        try {
            ai.caughtInvocationTargetException(ncm, ctor, "bar", ite);
        } catch (PicoCompositionException e) {
            assertSame("foo", e.getCause().getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    private static class MyAbstractInjector extends AbstractInjector {

        public MyAbstractInjector(final Object key,
                                   final Class impl,
                                  final Parameter[] parameters,
                                  final ComponentMonitor monitor,
                                  final boolean useNames) {
            super(key, impl, monitor, useNames, new ConstructorParameters(parameters));
        }

        @Override
        public void verify(final PicoContainer container) throws PicoCompositionException {
                }

        @Override
		public Object getComponentInstance(final PicoContainer container, final Type into) throws PicoCompositionException {
            return null;
        }

        @Override
		public String getDescriptor() {
            return null;
        }
    }


    public static class TestObject {
    	public String someField;

    	public TestObject(final String someValue) {

    	}

    	public void setSomething(final String value) {

    	}
    }

    @Test
    public void testAmbiguousComponentExceptionMessageWithNoAccsesibleObjectOrNoComponent() {
    	Generic<String> generic = Generic.get(String.class);
    	Object[] keys = new Object[] {String.class, "fwibble", "fribbit"};
    	AmbiguousComponentResolutionException acre = new AmbiguousComponentResolutionException(generic, keys);
    	assertTrue("Got " + acre.getMessage(),
    			acre.getMessage().contains("<not-specified> needs a 'java.lang.String' injected through : <unknown>'," +
    					" but there are too many choices to inject. These:[class java.lang.String, fwibble, fribbit]"));
    }

    @Test
    public void testAmbiguousComponentExceptionMessageWithComponentButNoAccessibleObject() {
    	Generic<String> generic = Generic.get(String.class);
    	Object[] keys = new Object[] {String.class, "fwibble", "fribbit"};
    	AmbiguousComponentResolutionException acre = new AmbiguousComponentResolutionException(generic, keys);
    	acre.setComponent(TestObject.class);
    	assertTrue("Got " + acre.getMessage(),
    			acre.getMessage().contains("com.picocontainer.injectors.AbstractInjectorTestCase$TestObject needs a 'java.lang.String' injected through : <unknown>'," +
    					" but there are too many choices to inject. These:[class java.lang.String, fwibble, fribbit]"));
    }


    @Test
    public void testAmbiguousComponentExceptionMessageWithConstructorAccessibleObject() {
    	Generic<String> generic = Generic.get(String.class);
    	Object[] keys = new Object[] {String.class, "fwibble", "fribbit"};
    	AccessibleObject accessibleObject = TestObject.class.getConstructors()[0];

    	AmbiguousComponentResolutionException acre = new AmbiguousComponentResolutionException(generic, keys);
    	acre.setComponent(TestObject.class);
    	acre.setMember(accessibleObject);

    	assertTrue("Got " + acre.getMessage(),
    			acre.getMessage().contains("class com.picocontainer.injectors.AbstractInjectorTestCase$TestObject needs a 'java.lang.String' injected into constructor " +
    					"'public com.picocontainer.injectors.AbstractInjectorTestCase$TestObject(java.lang.String)'"));

    }

    @Test
    public void testAmbiguousComponentExceptionMessageWithConstructorAccessibleObjectAndParameterNumber() {
    	Generic<String> generic = Generic.get(String.class);
    	Object[] keys = new Object[] {String.class, "fwibble", "fribbit"};
    	AccessibleObject accessibleObject = TestObject.class.getConstructors()[0];

    	AmbiguousComponentResolutionException acre = new AmbiguousComponentResolutionException(generic, keys);
    	acre.setComponent(TestObject.class);
    	acre.setMember(accessibleObject);
    	acre.setParameterNumber(0);

    	assertTrue("Got " + acre.getMessage(),
    			acre.getMessage().contains("class com.picocontainer.injectors.AbstractInjectorTestCase$TestObject needs a 'java.lang.String'" +
    					" injected into parameter #0 (zero based index) of constructor " +
    					"'public com.picocontainer.injectors.AbstractInjectorTestCase$TestObject(java.lang.String)'"));

    }

    @Test
    public void testAmbiguousComponentExceptionMessageWithFieldAccessibleObject() throws NoSuchFieldException, SecurityException {
    	Generic<String> generic = Generic.get(String.class);
    	Object[] keys = new Object[] {String.class, "fwibble", "fribbit"};
    	AccessibleObject accessibleObject = TestObject.class.getField("someField");

    	AmbiguousComponentResolutionException acre = new AmbiguousComponentResolutionException(generic, keys);
    	acre.setComponent(TestObject.class);
    	acre.setMember(accessibleObject);


    	assertTrue("Got " + acre.getMessage(),
    			acre.getMessage().contains("needs a 'java.lang.String' injected into field " +
    					"'public java.lang.String com.picocontainer.injectors.AbstractInjectorTestCase$TestObject.someField'"));
    }

    @Test
    public void testAmbiguousComponentExceptionMessageWithMethodAccessibleObject() throws SecurityException, NoSuchMethodException {
    	Generic<String> generic = Generic.get(String.class);
    	Object[] keys = new Object[] {String.class, "fwibble", "fribbit"};
    	AccessibleObject accessibleObject = TestObject.class.getMethod("setSomething", String.class);

    	AmbiguousComponentResolutionException acre = new AmbiguousComponentResolutionException(generic, keys);
    	acre.setComponent(TestObject.class);
    	acre.setMember(accessibleObject);


    	assertTrue("Got " + acre.getMessage(),
    			acre.getMessage().contains("needs a 'java.lang.String' injected into method " +
    					"'public void com.picocontainer.injectors.AbstractInjectorTestCase$TestObject.setSomething(java.lang.String)'"));
    }


}
