/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.annotations.Inject;
import com.picocontainer.injectors.AbstractInjector;
import com.picocontainer.injectors.MultiInjection;

/**
 * @author Paul Hammant
 */
public class MultiInjectionTestCase {

    public static class Bar {
    }
    public static class Baz {
    }
    public static class Foo {
        private final Bar bar;
        private Baz baz;

        public Foo(final Bar bar) {
            this.bar = bar;
        }

        public void setBaz(final Baz baz) {
            this.baz = baz;
        }
    }

    public static class Foo2 {
        private final Bar bar;
        private Baz baz;

        public Foo2(final Bar bar) {
            this.bar = bar;
        }

        public void injectBaz(final Baz baz) {
            this.baz = baz;
        }
    }

    public static class Foo3 {
        private final Bar bar;
        private Baz baz;

        public Foo3(final Bar bar) {
            this.bar = bar;
        }

        @Inject
        public void fjshdfkjhsdkfjh(final Baz baz) {
            this.baz = baz;
        }
    }

    @Test public void testComponentWithCtorAndSetterDiCanHaveAllDepsSatisfied() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(new MultiInjection());
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo.class);
        Foo foo = dpc.getComponent(Foo.class);
        assertNotNull(foo);
        assertNotNull(foo.bar);
        assertNotNull(foo.baz);
    }

    @Test public void testComponentWithCtorAndSetterDiCanHaveAllDepsSatisfiedWithANonSetInjectMethod() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(new MultiInjection("inject"));
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo2.class);
        Foo2 foo = dpc.getComponent(Foo2.class);
        assertNotNull(foo);
        assertNotNull(foo.bar);
        assertNotNull(foo.baz);
    }

    @Test public void testComponentWithCtorAndMethodAnnotatedDiCanHaveAllDepsSatisfied() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(new MultiInjection());
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo3.class);
        Foo3 foo = dpc.getComponent(Foo3.class);
        assertNotNull(foo);
        assertNotNull(foo.bar);
        assertNotNull(foo.baz);
    }


    @Test public void testComponentWithCtorAndSetterDiCanNoteMissingSetterDependency() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(new MultiInjection());
        dpc.addComponent(Bar.class);
        dpc.addComponent(Foo.class);
        try {
            Foo foo = dpc.getComponent(Foo.class);
        } catch (AbstractInjector.UnsatisfiableDependenciesException e) {
            String message = e.getMessage().replace("com.picocontainer.injectors.MultiInjectionTestCase$", "");

            assertEquals("Foo has unsatisfied dependencies [class Baz] for members [public void Foo.setBaz(Baz)] from " + dpc, message);
        }
    }

}