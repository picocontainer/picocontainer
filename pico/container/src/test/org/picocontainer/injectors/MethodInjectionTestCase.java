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

import java.lang.reflect.Method;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import static org.picocontainer.Characteristics.USE_NAMES;
import org.picocontainer.annotations.Nullable;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;

public class MethodInjectionTestCase {

    public static interface IFoo {
        void inject(Bar bar, Integer num);
    }
    public static class Foo implements IFoo {
        private Bar bar;
        private Integer num;

        public void inject(Bar bar, Integer num) {
            this.bar = bar;
            this.num = num;
        }
    }

    public static class Bar {
        public Bar() {
        }
    }

    @Test public void testMethodInjection() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new MethodInjection(), new NullLifecycleStrategy(), new EmptyPicoContainer());
        pico.addComponent(123);
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("MethodInjector-class org.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testMethodInjectionViaMethodDef() {
        Method mthd = Foo.class.getMethods()[0];
        DefaultPicoContainer pico = new DefaultPicoContainer(new MethodInjection(mthd), new NullLifecycleStrategy(), new EmptyPicoContainer());
        pico.addComponent(123);
        pico.addComponent(Foo.class);
        pico.addComponent(new Bar());
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("ReflectionMethodInjector["+mthd+"]-class org.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testMethodInjectionViaMethodDefViaInterface() {
        Method mthd = IFoo.class.getMethods()[0];
        DefaultPicoContainer pico = new DefaultPicoContainer(new MethodInjection(mthd), new NullLifecycleStrategy(), new EmptyPicoContainer());
        pico.addComponent(123);
        pico.addComponent(Foo.class);
        pico.addComponent(new Bar());
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("ReflectionMethodInjector["+mthd+"]-class org.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }


    @Test public void testMethodInjectionViaCharacteristics() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new NullLifecycleStrategy(), new EmptyPicoContainer());
        pico.addComponent(123);
        pico.as(Characteristics.METHOD_INJECTION).addComponent(Foo.class);
        pico.addComponent(Bar.class);
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("MethodInjector-class org.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testMethodInjectionViaAdapter() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new MethodInjection());
        pico.addComponent(123);
        pico.addAdapter(new MethodInjector(Foo.class, Foo.class, null, new NullComponentMonitor(), "inject", false));
        pico.addComponent(Bar.class);
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("MethodInjector-class org.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testMethodInjectionByBuilder() {
        MutablePicoContainer pico = new PicoBuilder().withMethodInjection().build();
        pico.addComponent(123);
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals("MethodInjector-class org.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

    public static class Foo2 implements IFoo {
        private Bar bar;
        private Integer num;

        public void inject(Bar bar, @Nullable Integer num) {
            this.bar = bar;
            this.num = num;
        }
    }

    @Test public void testMethodInjectionWithAllowedNullableParam() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new MethodInjection(), new NullLifecycleStrategy(), new EmptyPicoContainer());
        pico.addComponent(Foo2.class);
        pico.addComponent(Bar.class);
        Foo2 foo = pico.getComponent(Foo2.class);
        assertNotNull(foo.bar);
        assertTrue(foo.num == null);
        assertEquals("MethodInjector-class org.picocontainer.injectors.MethodInjectionTestCase$Foo2", pico.getComponentAdapter(Foo2.class).toString());
    }

    @Test public void testMethodInjectionWithDisallowedNullableParam() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new MethodInjection());
        pico.addComponent(Foo.class);
        pico.addComponent(Bar.class);
        try {
            Foo foo = pico.getComponent(Foo.class);
            fail("should have barfed");
        } catch (SingleMemberInjector.ParameterCannotBeNullException e) {
            assertEquals("num", e.getParameterName());
            assertTrue(e.getMessage().indexOf("Parameter 1") != -1);
            assertTrue(e.getMessage().indexOf(Foo.class.getMethods()[0].toString()) != -1);
        }
    }

    @Test public void testMethodInjectionWithIntegerParamCanBeconvertedFromString() {
        DefaultPicoContainer pico = new DefaultPicoContainer(new MethodInjection(), new NullLifecycleStrategy(), new EmptyPicoContainer());
        pico.as(USE_NAMES).addComponent(Foo.class);
        pico.addComponent(Bar.class);
        pico.addComponent("num", "123");
        Foo foo = pico.getComponent(Foo.class);
        assertNotNull(foo.bar);
        assertNotNull(foo.num);
        assertEquals(123, (int)foo.num);
        assertEquals("MethodInjector-class org.picocontainer.injectors.MethodInjectionTestCase$Foo", pico.getComponentAdapter(Foo.class).toString());
    }

}