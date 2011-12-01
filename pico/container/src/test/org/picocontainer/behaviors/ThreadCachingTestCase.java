/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;

public class ThreadCachingTestCase {

    public static class Foo {
        public Foo(StringBuilder sb) {
            sb.append("<Foo");
        }
    }

    public static class Bar {
        private final Foo foo;
        public Bar(StringBuilder sb, Foo foo) {
            this.foo = foo;
            sb.append("<Bar");
        }
    }

    @Test public void testThatForASingleThreadTheBehaviorIsTheSameAsPlainCaching() {

        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        DefaultPicoContainer child = new DefaultPicoContainer(parent, new NullLifecycleStrategy(), new ThreadCaching());

        parent.addComponent(StringBuilder.class);
        child.addComponent(Foo.class);

        StringBuilder sb = parent.getComponent(StringBuilder.class);
        Foo foo = child.getComponent(Foo.class);
        Foo foo2 = child.getComponent(Foo.class);
        assertNotNull(foo);
        assertNotNull(foo2);
        assertEquals(foo,foo2);
        assertEquals("<Foo", sb.toString());
        assertEquals("ThreadCached:ConstructorInjector-class org.picocontainer.behaviors.ThreadCachingTestCase$Foo", child.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testThatTwoThreadsHaveSeparatedCacheValues() {

        final Foo[] foos = new Foo[4];

        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        final DefaultPicoContainer child = new DefaultPicoContainer(parent, new NullLifecycleStrategy(), new ThreadCaching());

        parent.addComponent(StringBuilder.class);
        child.addComponent(Foo.class);

        StringBuilder sb = parent.getComponent(StringBuilder.class);
        foos[0] = child.getComponent(Foo.class);

        Thread thread = new Thread() {
            public void run() {
                foos[1] = child.getComponent(Foo.class);
                foos[3] = child.getComponent(Foo.class);
            }
        };
        thread.start();
        foos[2] = child.getComponent(Foo.class);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        assertNotNull(foos[0]);
        assertNotNull(foos[1]);
        assertNotNull(foos[2]);
        assertNotNull(foos[3]);
        assertSame(foos[0],foos[2]);
        assertEquals(foos[1],foos[3]);
        assertFalse(foos[0] == foos[1]);
        assertEquals("<Foo<Foo", sb.toString());
        assertEquals("ThreadCached:ConstructorInjector-class org.picocontainer.behaviors.ThreadCachingTestCase$Foo", child.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testThatTwoThreadsHaveSeparatedCacheValuesForThreeScopeScenario() {

        final Foo[] foos = new Foo[4];
        final Bar[] bars = new Bar[4];

        DefaultPicoContainer appScope = new DefaultPicoContainer(new Caching());
        final DefaultPicoContainer sessionScope = new DefaultPicoContainer(appScope, new NullLifecycleStrategy(), new ThreadCaching());
        final DefaultPicoContainer requestScope = new DefaultPicoContainer(sessionScope, new NullLifecycleStrategy(), new ThreadCaching());

        appScope.addComponent(StringBuilder.class);
        sessionScope.addComponent(Foo.class);
        requestScope.addComponent(Bar.class);

        StringBuilder sb = appScope.getComponent(StringBuilder.class);
        foos[0] = sessionScope.getComponent(Foo.class);
        bars[0] = requestScope.getComponent(Bar.class);

        Thread thread = new Thread() {
            public void run() {
                foos[1] = sessionScope.getComponent(Foo.class);
                bars[1] = requestScope.getComponent(Bar.class);
                foos[3] = sessionScope.getComponent(Foo.class);
                bars[3] = requestScope.getComponent(Bar.class);
            }
        };
        thread.start();
        foos[2] = sessionScope.getComponent(Foo.class);
        bars[2] = requestScope.getComponent(Bar.class);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        assertSame(bars[0],bars[2]);
        assertEquals(bars[1],bars[3]);
        assertFalse(bars[0] == bars[1]);
        assertSame(bars[0].foo,foos[0]);
        assertSame(bars[1].foo,foos[1]);
        assertSame(bars[2].foo,foos[2]);
        assertSame(bars[3].foo,foos[3]);
        assertEquals("<Foo<Bar<Foo<Bar", sb.toString());
        assertEquals("ThreadCached:ConstructorInjector-class org.picocontainer.behaviors.ThreadCachingTestCase$Foo", sessionScope.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testThatTwoThreadsHaveSeparatedCacheValuesWithInstanceRegistrationAndClassLoadingPicoContainer() {

        final Foo[] foos = new Foo[4];

        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        parent.change(Characteristics.USE_NAMES);
        final DefaultClassLoadingPicoContainer child = new DefaultClassLoadingPicoContainer(new ThreadCaching(), new NullLifecycleStrategy(), parent, this.getClass().getClassLoader(), new NullComponentMonitor());
        child.change(Characteristics.USE_NAMES);

        parent.addComponent(StringBuilder.class);
        child.addComponent(Foo.class);
        child.addComponent("hello");

        StringBuilder sb = parent.getComponent(StringBuilder.class);
        foos[0] = child.getComponent(Foo.class);

        Thread thread = new Thread() {
            public void run() {
                foos[1] = child.getComponent(Foo.class);
                foos[3] = child.getComponent(Foo.class);
            }
        };
        thread.start();
        foos[2] = child.getComponent(Foo.class);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        assertNotNull(foos[0]);
        assertNotNull(foos[1]);
        assertNotNull(foos[2]);
        assertNotNull(foos[3]);
        assertSame(foos[0],foos[2]);
        assertEquals(foos[1],foos[3]);
        assertFalse(foos[0] == foos[1]);
        assertEquals("<Foo<Foo", sb.toString());
        assertEquals("ThreadCached:ConstructorInjector-class org.picocontainer.behaviors.ThreadCachingTestCase$Foo", child.getComponentAdapter(Foo.class).toString());
    }




}