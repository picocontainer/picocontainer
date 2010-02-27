/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.defaults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.testmodel.Touchable;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.adapters.InstanceAdapter;
import org.picocontainer.adapters.NullCA;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.ConstructorInjector;

import java.lang.reflect.Type;
import java.lang.annotation.Annotation;

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

        public One(Two two) {
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

        public Three(Two two, String string, Integer integer) {
            this.two = two;
            this.string = string;
            this.integer = integer;
        }

        public Three(Two two, String string) {
            this.two = two;
            this.string = string;
            integer = null;
        }

        public Three(Two two) {
            this.two = two;
            string = null;
            integer = null;
        }
    }

    private class CountingConstructorInjector extends ConstructorInjector {

        public CountingConstructorInjector(Class<?> componentKey, Class<?> componentImplementation) {
            super(componentKey, componentImplementation, null);
        }

        protected CtorAndAdapters getGreediestSatisfiableConstructor(PicoContainer container) throws PicoCompositionException {
            CtorAndAdapters adapters = super.getGreediestSatisfiableConstructor(container);
            parms = adapters.getParameters();
            injecteeAdapters = adapters.getInjecteeAdapters();
            return adapters;
        }

        protected Parameter[] createDefaultParameters(Type[] parameters) {
            Parameter[] componentParameters = new Parameter[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                componentParameters[i] = new CountingComponentParameter();

            }
            return componentParameters;
        }

    }

    private class CountingComponentParameter extends ComponentParameter {
        public int hashCode() {
            return ResolveAdapterReductionTestCase.super.hashCode();
        }

        public boolean equals(Object o) {
            return true;
        }

        protected <T> ComponentAdapter<T> resolveAdapter(PicoContainer container, ComponentAdapter adapter, Class<T> expectedType, NameBinding expectedNameBinding, boolean useNames, Annotation binding) {
            if (expectedType == Two.class || expectedType == Touchable.class) {
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
