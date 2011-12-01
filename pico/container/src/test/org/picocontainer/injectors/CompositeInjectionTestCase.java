/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import static org.picocontainer.injectors.NamedFieldInjection.injectionFieldNames;
import org.picocontainer.annotations.Inject;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.lifecycle.NullLifecycleStrategy;

/**
 * @author Paul Hammant
 */
public class CompositeInjectionTestCase {

    public static class Bar {
    }
    public static class Baz {
    }

    public static class Foo {
        private final Bar bar;
        private Baz baz;

        public Foo(Bar bar) {
            this.bar = bar;
        }

        public void setBaz(Baz baz) {
            this.baz = baz;
        }
    }

    public static class Foo2 {
        private final Bar bar;
        private Baz baz;

        public Foo2(Bar bar) {
            this.bar = bar;
        }

        public void injectBaz(Baz baz) {
            this.baz = baz;
        }
    }

    public static class Foo3 {
        private final Bar bar;
        private Baz baz;

        public Foo3(Bar bar) {
            this.bar = bar;
        }

        @Inject
        public void fjshdfkjhsdkfjh(Baz baz) {
            this.baz = baz;
        }
    }
    public static class Foo4 {
        private final Bar bar;
        private String one;
        private String two;

        public Foo4(Bar bar) {
            this.bar = bar;
        }

    }

    @Test public void testComponentWithCtorAndSetterDiCanHaveAllDepsSatisfied() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(), new NullLifecycleStrategy(), new CompositeInjection(new ConstructorInjection(), new SetterInjection()));
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo.class);
        Foo foo = dpc.getComponent(Foo.class);
        assertNotNull(foo);
        assertNotNull(foo.bar);
        assertNotNull(foo.baz);
    }

    @Test public void testComponentWithCtorAndSetterDiCanHaveAllDepsSatisfiedWithANonSetInjectMethod() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(), new NullLifecycleStrategy(), new CompositeInjection(new ConstructorInjection(), new SetterInjection("inject")));
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo2.class);
        Foo2 foo = dpc.getComponent(Foo2.class);
        assertNotNull(foo);
        assertNotNull(foo.bar);
        assertNotNull(foo.baz);
    }

    @Test public void testComponentWithCtorAndMethodAnnotatedDiCanHaveAllDepsSatisfied() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(), new NullLifecycleStrategy(), new CompositeInjection(new ConstructorInjection(), new AnnotatedMethodInjection()));
        dpc.addComponent(Bar.class);
        dpc.addComponent(Baz.class);
        dpc.addComponent(Foo3.class);
        Foo3 foo = dpc.getComponent(Foo3.class);
        assertNotNull(foo);
        assertNotNull(foo.bar);
        assertNotNull(foo.baz);
    }


    @Test public void testComponentWithCtorAndNamedFieldWorkToegether() throws NoSuchMethodException {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(), new NullLifecycleStrategy(), new CompositeInjection(new ConstructorInjection(), new NamedFieldInjection()));
        dpc.addComponent(Bar.class);
        dpc.addConfig("one", "1");
        dpc.addConfig("two", "2");
        dpc.as(injectionFieldNames("one", "two")).addComponent(Foo4.class);
        Foo4 foo4 = dpc.getComponent(Foo4.class);
        assertNotNull(foo4);
        assertNotNull(foo4.bar);
        assertNotNull(foo4.one);
        assertEquals("1", foo4.one);
        assertNotNull(foo4.two);
        assertEquals("2", foo4.two);
    }

    @Test public void testWithNonNullLifecycleStrategy() {
        DefaultPicoContainer dpc = new DefaultPicoContainer(
                new EmptyPicoContainer(),
                new NonNullLifecycleStrategy(),
                new CompositeInjection(new ConstructorInjection(), new AnnotatedMethodInjection())
       );
        dpc.addComponent(Bar.class);
        assertNotNull(dpc.getComponent(Bar.class));
    }

    private static class NonNullLifecycleStrategy implements LifecycleStrategy {
        public void start(Object component) {
        }

        public void stop(Object component) {
        }

        public void dispose(Object component) {
        }

        public boolean hasLifecycle(Class<?> type) {
            return false;
        }

        public boolean isLazy(ComponentAdapter<?> adapter) {
            return false;
        }
    }
}