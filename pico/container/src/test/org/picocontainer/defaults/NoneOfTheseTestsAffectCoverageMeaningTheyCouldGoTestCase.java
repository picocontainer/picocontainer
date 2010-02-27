/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.defaults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.NameBinding;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.testmodel.DependsOnTouchable;
import org.picocontainer.testmodel.SimpleTouchable;
import org.picocontainer.testmodel.Touchable;
import org.picocontainer.testmodel.Webster;

public final class NoneOfTheseTestsAffectCoverageMeaningTheyCouldGoTestCase {

    //TODO - move to AbstractComponentRegistryTestCase
    @Test public void testGetComponentSpecification() throws PicoCompositionException {
        DefaultPicoContainer pico = new DefaultPicoContainer();

        assertNull(pico.getComponentAdapter(Touchable.class, (NameBinding) null));
        pico.addComponent(SimpleTouchable.class);
        assertNotNull(pico.getComponentAdapter(SimpleTouchable.class, (NameBinding) null));
        assertNotNull(pico.getComponentAdapter(Touchable.class,(NameBinding)  null));
    }


    //TODO move
    @Test public void testMultipleImplementationsAccessedThroughKey()
            throws PicoCompositionException
    {
        SimpleTouchable Touchable1 = new SimpleTouchable();
        SimpleTouchable Touchable2 = new SimpleTouchable();
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent("Touchable1", Touchable1);
        pico.addComponent("Touchable2", Touchable2);
        pico.addComponent("fred1", DependsOnTouchable.class, new ComponentParameter("Touchable1"));
        pico.addComponent("fred2", DependsOnTouchable.class, new ComponentParameter("Touchable2"));

        DependsOnTouchable fred1 = (DependsOnTouchable) pico.getComponent("fred1");
        DependsOnTouchable fred2 = (DependsOnTouchable) pico.getComponent("fred2");

        assertFalse(fred1 == fred2);
        assertSame(Touchable1, fred1.getTouchable());
        assertSame(Touchable2, fred2.getTouchable());
    }

    //TODO - move
    @Test public void testRegistrationByName() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();

        Webster one = new Webster(new ArrayList());
        Touchable two = new SimpleTouchable();

        pico.addComponent("one", one);
        pico.addComponent("two", two);

        assertEquals("Wrong number of comps in the internals", 2, pico.getComponents().size());

        assertEquals("Looking up one Touchable", one, pico.getComponent("one"));
        assertEquals("Looking up two Touchable", two, pico.getComponent("two"));

        assertTrue("Object one the same", one == pico.getComponent("one"));
        assertTrue("Object two the same", two == pico.getComponent("two"));

        assertEquals("Lookup of unknown key should return null", null, pico.getComponent("unknown"));
    }

    @Test public void testRegistrationByNameAndClassWithResolving() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();

        pico.addComponent(List.class, new ArrayList());
        pico.addComponent("one", Webster.class);
        pico.addComponent("two", SimpleTouchable.class);

        assertEquals("Wrong number of comps in the internals", 3, pico.getComponents().size());

        assertNotNull("Object one the same", pico.getComponent("one"));
        assertNotNull("Object two the same", pico.getComponent("two"));

        assertNull("Lookup of unknown key should return null", pico.getComponent("unknown"));
    }

    @Test public void testDuplicateRegistrationWithTypeAndObject() throws PicoCompositionException {
        DefaultPicoContainer pico = new DefaultPicoContainer();

        pico.addComponent(SimpleTouchable.class);
        try {
            pico.addComponent(SimpleTouchable.class, new SimpleTouchable());
            fail("Should have barfed with dupe registration");
        } catch (PicoCompositionException e) {
            // expected
            assertTrue(e.getMessage().startsWith("Duplicate"));
            assertTrue(e.getMessage().indexOf(SimpleTouchable.class.getName()) > 0);
        }
    }


    @Test public void testComponentRegistrationMismatch() throws PicoCompositionException {
        MutablePicoContainer pico = new DefaultPicoContainer();

        try {
            pico.addComponent(List.class, SimpleTouchable.class);
        } catch (ClassCastException e) {
            // not worded in message
            assertTrue(e.getMessage().indexOf(List.class.getName()) > 0);
            assertTrue(e.getMessage().indexOf(SimpleTouchable.class.getName()) == 0);
        }

    }

    interface Animal {

        String getFood();
    }

    public static class Dino implements Animal {
        final String food;

        public Dino(String food) {
            this.food = food;
        }

        public String getFood() {
            return food;
        }
    }

    public static class Dino2 extends Dino {
        public Dino2(int number) {
            super(String.valueOf(number));
        }
    }

    public static class Dino3 extends Dino {
        public Dino3(String a, String b) {
            super(a + b);
        }
    }

    public static class Dino4 extends Dino {
        public Dino4(String a, int n, String b, Touchable Touchable) {
            super(a + n + b + " " + Touchable.getClass().getName());
        }
    }

    @Test public void testParameterCanBePassedToConstructor() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Animal.class,
                Dino.class,
                new ConstantParameter("bones"));

        Animal animal = pico.getComponent(Animal.class);
        assertNotNull("Component not null", animal);
        assertEquals("bones", animal.getFood());
    }

    @Test public void testParameterCanBePrimitive() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Animal.class, Dino2.class, new ConstantParameter(22));

        Animal animal = pico.getComponent(Animal.class);
        assertNotNull("Component not null", animal);
        assertEquals("22", animal.getFood());
    }

    @Test public void testMultipleParametersCanBePassed() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Animal.class, Dino3.class, new ConstantParameter("a"),
                new ConstantParameter("b"));

        Animal animal = pico.getComponent(Animal.class);
        assertNotNull("Component not null", animal);
        assertEquals("ab", animal.getFood());

    }

    @Test public void testParametersCanBeMixedWithComponentsCanBePassed() throws Exception {
        DefaultPicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Touchable.class, SimpleTouchable.class);
        pico.addComponent(Animal.class, Dino4.class, new ConstantParameter("a"),
                new ConstantParameter(3),
                new ConstantParameter("b"),
                ComponentParameter.DEFAULT);

        Animal animal = pico.getComponent(Animal.class);
        assertNotNull("Component not null", animal);
        assertEquals("a3b org.picocontainer.testmodel.SimpleTouchable", animal.getFood());
    }

}
