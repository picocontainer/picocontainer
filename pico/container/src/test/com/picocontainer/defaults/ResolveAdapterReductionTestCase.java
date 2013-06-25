/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package com.picocontainer.defaults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.annotation.Annotation;

import org.junit.Test;
import com.picocontainer.testmodel.Touchable;

import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.NameBinding;
import com.picocontainer.Parameter;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.adapters.InstanceAdapter;
import com.picocontainer.injectors.ConstructorInjection;
import com.picocontainer.parameters.ComponentParameter;

/**
 * @author Paul Hammant
 */
public class ResolveAdapterReductionTestCase {

    int resolveAdapterCalls;
    int getCompInstCalls;
    private Parameter[] parms;
    private ComponentAdapter[] injecteeAdapters;

    @Test
    public void testThatResolveAdapterCanBeDoneOnceForASituationWhereItWasPreviouslyDoneAtLeastTwice() throws Exception {
        resolveAdapterCalls = 0;
        DefaultPicoContainer pico = new DefaultPicoContainer(new ConstructorInjection());
        pico.addAdapter(new CountingConstructorInjector(One.class, One.class));
        pico.addComponent(new Two());
        long start = System.currentTimeMillis();
        for (int x = 0; x < 30000; x++) {
            One one = pico.getComponent(One.class);
            assertNotNull(one);
            assertNotNull(one.two);
            assertEquals("resolveAdapter for 'Two' should only be called once, regardless of how many getComponents there are",
                    1, resolveAdapterCalls);
        }
        System.out.println("ResolveAdapterReductionTestCase elapsed: " + (System.currentTimeMillis() - start));
        assertNotNull(parms);
        assertEquals(1, parms.length);
        assertEquals(true, parms[0] instanceof CountingComponentParameter);
        assertNotNull(injecteeAdapters);
        assertEquals(1, injecteeAdapters.length);
        assertEquals(true, injecteeAdapters[0] instanceof InstanceAdapter);
        //System.out.println("--> " + getCompInstCalls);
    }

    @Test
    public void testThatResolveAdapterCallsAreNotDuplicatedForMultipleConstructorsInTheSameComponent() throws Exception {
        resolveAdapterCalls = 0;
        DefaultPicoContainer pico = new DefaultPicoContainer(new ConstructorInjection());
        // 'Three', in addition to a 'Two', requires a string, and an int for two of the longer constructors ....
        pico.addAdapter(new CountingConstructorInjector(Three.class, Three.class));
        // .. but we ain't going to provide them, forcing the smallest constructor to be used.
        pico.addComponent(new Two());
        long start = System.currentTimeMillis();
        for (int x = 0; x < 30000; x++) {
            Three three = pico.getComponent(Three.class);
            assertNotNull(three);
            assertNotNull(three.two);
            assertNull(three.string);
            assertNull(three.integer);

            // if we did not cache the results of the longer (unsatisfiable) constructors, then we'd be doing
            // resolveAdapter(..) more than once.  See ConstructorInjector.ResolverKey.
            assertEquals("resolveAdapter for 'Two' should only be called once, regardless of how many getComponents there are",
                    1, resolveAdapterCalls);
        }
        System.out.println("ResolveAdapterReductionTestCase elapsed: " + (System.currentTimeMillis() - start));
    }

    public static class One {
        private final Two two;

        public One(final Two two) {
            this.two = two;
        }
    }

    public static class Two {
        public Two() {
        }
    }

    public static class Three {
        private final Two two;
        private final String string;
        private final Integer integer;

        public Three(final Two two, final String string, final Integer integer) {
            this.two = two;
            this.string = string;
            this.integer = integer;
        }

        public Three(final Two two, final String string) {
            this.two = two;
            this.string = string;
            integer = null;
        }

        public Three(final Two two) {
            this.two = two;
            string = null;
            integer = null;
        }
    }

    @SuppressWarnings({"serial", "rawtypes", "unchecked"})
	private class CountingConstructorInjector extends ConstructorInjection.ConstructorInjector {

		public CountingConstructorInjector(final Class<?> key, final Class<?> impl) {
            super(key, impl);
        }

        @Override
		protected CtorAndAdapters getGreediestSatisfiableConstructor(final PicoContainer container) throws PicoCompositionException {
            CtorAndAdapters adapters = super.getGreediestSatisfiableConstructor(container);
            parms = adapters.getParameters();
            injecteeAdapters = adapters.getInjecteeAdapters();
            return adapters;
        }

        @Override
		protected Parameter[] createDefaultParameters(final int length) {
            Parameter[] componentParameters = new Parameter[length];
            for (int i = 0; i < length; i++) {
                componentParameters[i] = new CountingComponentParameter();

            }
            return componentParameters;
        }

    }

    @SuppressWarnings("serial")
	private class CountingComponentParameter extends ComponentParameter {
        @Override
		public int hashCode() {
            return ResolveAdapterReductionTestCase.super.hashCode();
        }

        @Override
		public boolean equals(final Object o) {
            return true;
        }

        @Override
		protected <T> ComponentAdapter<T> resolveAdapter(final PicoContainer container, final ComponentAdapter<?> adapter, final Generic<T> expectedType, final NameBinding expectedNameBinding, final boolean useNames, final Annotation binding) {
            if (expectedType.getType() == Two.class || expectedType.getType() == Touchable.class) {
                resolveAdapterCalls++;
            }
            return super.resolveAdapter(container, adapter, expectedType, expectedNameBinding, useNames, binding);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    public static class FooNameBinding implements NameBinding {
        public String getName() {
            return "";
        }
    }

}
