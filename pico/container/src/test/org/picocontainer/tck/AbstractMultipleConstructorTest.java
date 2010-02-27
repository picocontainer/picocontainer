/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.tck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoException;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;


/**
 * @author Aslak Helles&oslash;y
 */
public abstract class AbstractMultipleConstructorTest {

    protected abstract MutablePicoContainer createPicoContainer();

    public static final class Multi {
        public final String message;

        public Multi(One one, Two two, Three three) {
            message = "one two three";
        }

        public Multi(One one, Two two) {
            message = "one two";
        }

        public Multi(Two two, One one) {
            message = "two one";
        }

        public Multi(Two two, Three three) {
            message = "two three";
        }

        public Multi(Three three, One one) {
            message = "three one";
        }

        public Multi(One one, String string) {
            message = "one string";
        }

        public Multi(One one, int i) {
            message = "one int";
        }

        public Multi() {
            message = "none";
        }
    }

    public static class One {
    }

    public static class Two {
    }

    public static class Three {
    }


    @Test public void testStringWorks() throws PicoException {
        MutablePicoContainer pico = createPicoContainer();
        pico.addComponent(String.class);
        assertEquals("", pico.getComponent(String.class));
    }

    @Test public void testMultiWithOnlySmallSatisfiedDependencyWorks() throws PicoException {
        MutablePicoContainer pico = createPicoContainer();
        pico.addComponent(Multi.class);
        pico.addComponent(One.class);
        pico.addComponent(Three.class);

        Multi multi = pico.getComponent(Multi.class);
        assertEquals("three one", multi.message);
    }

    @Test public void testMultiWithBothSatisfiedDependencyWorks() throws PicoException {
        MutablePicoContainer pico = createPicoContainer();
        pico.addComponent(Multi.class);
        pico.addComponent(One.class);
        pico.addComponent(Two.class);
        pico.addComponent(Three.class);

        Multi multi = pico.getComponent(Multi.class);
        assertEquals("one two three", multi.message);
    }

    @Test public void testMultiWithTwoEquallyBigSatisfiedDependenciesFails() throws PicoException {
        MutablePicoContainer pico = createPicoContainer();
        pico.addComponent(Multi.class);
        pico.addComponent(One.class);
        pico.addComponent(Two.class);

        try {
            pico.getComponent(Multi.class);
            fail();
        } catch (PicoCompositionException e) {
            assertEquals("3 satisfiable constructors is too many for 'class org.picocontainer.tck.AbstractMultipleConstructorTest$Multi'. Constructor List:[<init>(), <init>(org.picocontainer.tck.AbstractMultipleConstructorTest$One,org.picocontainer.tck.AbstractMultipleConstructorTest$Two), <init>(org.picocontainer.tck.AbstractMultipleConstructorTest$Two,org.picocontainer.tck.AbstractMultipleConstructorTest$One)]",
                    e.getMessage());
        }
    }

    @Test public void testMultiWithSatisfyingDependencyAndParametersWorks() throws PicoException {
        MutablePicoContainer pico = createPicoContainer();
        pico.addComponent("MultiOneTwo", Multi.class, ComponentParameter.DEFAULT,
                new ComponentParameter("Two"));
        pico.addComponent("MultiTwoOne", Multi.class, new ComponentParameter("Two"),
                ComponentParameter.DEFAULT);
        pico.addComponent("MultiOneString", Multi.class, ComponentParameter.DEFAULT,
                new ConstantParameter(""));
        pico.addComponent("MultiOneInt", Multi.class, ComponentParameter.DEFAULT,
                new ConstantParameter(5));
        pico.addComponent("MultiNone", Multi.class, Parameter.ZERO);
        pico.addComponent(One.class);
        pico.addComponent("Two", Two.class);

        Multi multiOneTwo = (Multi) pico.getComponent("MultiOneTwo");
        assertEquals("one two", multiOneTwo.message);
        Multi multiTwoOne = (Multi) pico.getComponent("MultiTwoOne");
        assertEquals("two one", multiTwoOne.message);
        Multi multiOneString = (Multi) pico.getComponent("MultiOneString");
        assertEquals("one string", multiOneString.message);
        Multi multiOneInt = (Multi) pico.getComponent("MultiOneInt");
        assertEquals("one int", multiOneInt.message);
        Multi multiNone = (Multi) pico.getComponent("MultiNone");
        assertEquals("none", multiNone.message);
    }
}
