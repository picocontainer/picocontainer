/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.lifecycle.NullLifecycleStrategy;

public class StoringTestCase {

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
        Storing storeCaching = new Storing();
        DefaultPicoContainer child = new DefaultPicoContainer(parent, new NullLifecycleStrategy(), storeCaching);

        parent.addComponent(StringBuilder.class);
        child.addComponent(Foo.class);

        StringBuilder sb = parent.getComponent(StringBuilder.class);
        Foo foo = child.getComponent(Foo.class);
        Foo foo2 = child.getComponent(Foo.class);
        assertNotNull(foo);
        assertNotNull(foo2);
        assertEquals(foo,foo2);
        assertEquals("<Foo", sb.toString());
        assertEquals("Stored:ConstructorInjector-class org.picocontainer.behaviors.StoringTestCase$Foo", child.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testThatTwoThreadsHaveSeparatedCacheValues() {

        final Foo[] foos = new Foo[4];
        final int[] sizes = new int[2];

        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        final Storing storing = new Storing();
        final DefaultPicoContainer child = new DefaultPicoContainer(parent, new NullLifecycleStrategy(), storing);

        parent.addComponent(StringBuilder.class);
        child.addComponent(Foo.class);

        StringBuilder sb = parent.getComponent(StringBuilder.class);
        assertEquals("store was not empty at outset for main thread", 0, storing.getCacheSize());
        foos[0] = child.getComponent(Foo.class);

        Thread thread = new Thread("other") {
            public void run() {
                sizes[0] = storing.getCacheSize();
                foos[1] = child.getComponent(Foo.class);
                foos[3] = child.getComponent(Foo.class);
                sizes[1] = storing.getCacheSize();
            }
        };
        thread.start();
        foos[2] = child.getComponent(Foo.class);
        assertEquals("store was not sized 1 at end for main thread", 1, storing.getCacheSize());

        sleepALittle();

        assertNotNull(foos[0]);
        assertNotNull(foos[1]);
        assertNotNull(foos[2]);
        assertNotNull(foos[3]);
        assertSame(foos[0],foos[2]);
        assertEquals(foos[1],foos[3]);
        assertFalse(foos[0] == foos[1]);
        assertEquals("<Foo<Foo", sb.toString());
        assertEquals("Stored:ConstructorInjector-class org.picocontainer.behaviors.StoringTestCase$Foo", child.getComponentAdapter(Foo.class).toString());

        assertEquals("store was not empty at outset for other thread", 0, sizes[0]);
        assertEquals("store was not sized 1 at end for other thread", 1, sizes[1]);
    }

    @Test public void testThatTwoThreadsHaveSeparatedCacheValuesForThreeScopeScenario() {

        final Foo[] foos = new Foo[4];
        final Bar[] bars = new Bar[4];

        DefaultPicoContainer appScope = new DefaultPicoContainer(new Caching());
        final DefaultPicoContainer sessionScope = new DefaultPicoContainer(appScope, new NullLifecycleStrategy(), new Storing());
        final DefaultPicoContainer requestScope = new DefaultPicoContainer(sessionScope, new NullLifecycleStrategy(), new Storing());

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
        sleepALittle();

        assertSame(bars[0],bars[2]);
        assertEquals(bars[1],bars[3]);
        assertFalse(bars[0] == bars[1]);
        assertSame(bars[0].foo,foos[0]);
        assertSame(bars[1].foo,foos[1]);
        assertSame(bars[2].foo,foos[2]);
        assertSame(bars[3].foo,foos[3]);
        assertEquals("<Foo<Bar<Foo<Bar", sb.toString());
        assertEquals("Stored:ConstructorInjector-class org.picocontainer.behaviors.StoringTestCase$Foo", sessionScope.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testThatCacheMapCanBeReUsedOnASubsequentThreadSimulatingASessionConcept() {

        final Foo[] foos = new Foo[4];

        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        final Storing storeCaching = new Storing();
        final DefaultPicoContainer child = new DefaultPicoContainer(parent, new NullLifecycleStrategy(), storeCaching);

        parent.addComponent(StringBuilder.class);
        child.addComponent(Foo.class);

        StringBuilder sb = parent.getComponent(StringBuilder.class);

        final Storing.StoreWrapper[] tmpMap = new Storing.StoreWrapper[1];
        Thread thread = new Thread() {
            public void run() {
                foos[0] = child.getComponent(Foo.class);
                foos[1] = child.getComponent(Foo.class);
                tmpMap[0] = storeCaching.getCacheForThread();

            }
        };
        thread.start();
        sleepALittle();
        thread = new Thread() {
            public void run() {
                storeCaching.putCacheForThread(tmpMap[0]);
                foos[2] = child.getComponent(Foo.class);
                foos[3] = child.getComponent(Foo.class);
                tmpMap[0] = storeCaching.getCacheForThread();

            }
        };
        thread.start();
        sleepALittle();

        assertNotNull(foos[0]);
        assertNotNull(foos[1]);
        assertNotNull(foos[2]);
        assertNotNull(foos[3]);
        assertSame(foos[0],foos[1]);
        assertSame(foos[1],foos[2]);
        assertSame(foos[2],foos[3]);
        assertEquals("<Foo", sb.toString());
        assertEquals("Stored:ConstructorInjector-class org.picocontainer.behaviors.StoringTestCase$Foo", child.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testThatCacheMapCanBeResetOnASubsequentThreadSimulatingASessionConcept() {


        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        final Storing storeCaching = new Storing();
        final DefaultPicoContainer child = new DefaultPicoContainer(parent, new NullLifecycleStrategy(), storeCaching);

        parent.addComponent(StringBuilder.class);
        child.addComponent(Foo.class);

        StringBuilder sb = parent.getComponent(StringBuilder.class);

        Foo one = child.getComponent(Foo.class);
        Foo two = child.getComponent(Foo.class);

        assertNotNull(one);
        assertNotNull(two);
        assertSame(one,two);

        assertTrue(storeCaching.resetCacheForThread() instanceof Storing.StoreWrapper);

        Foo three = child.getComponent(Foo.class);
        Foo four = child.getComponent(Foo.class);

        assertNotNull(three);
        assertNotNull(four);
        assertNotSame(one,three);
        assertSame(three,four);

        assertEquals("<Foo<Foo", sb.toString());
        assertEquals("Stored:ConstructorInjector-class org.picocontainer.behaviors.StoringTestCase$Foo", child.getComponentAdapter(Foo.class).toString());
    }

    @Test public void testThatCacheMapCanBeDisabledSimulatingAnEndedRequest() {

        DefaultPicoContainer parent = new DefaultPicoContainer(new Caching());
        final Storing storeCaching = new Storing();
        final DefaultPicoContainer child = new DefaultPicoContainer(parent, storeCaching);

        parent.addComponent(StringBuilder.class);
        child.addComponent(Foo.class);

        StringBuilder sb = parent.getComponent(StringBuilder.class);

        Foo one = child.getComponent(Foo.class);
        Foo two = child.getComponent(Foo.class);

        assertNotNull(one);
        assertNotNull(two);
        assertSame(one,two);

        storeCaching.invalidateCacheForThread();

        try {
            Foo three = child.getComponent(Foo.class);
            fail("should have barfed");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }


    private void sleepALittle() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
    }


}