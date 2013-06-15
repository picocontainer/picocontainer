/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the CyclicDependecy.
 */
public final class ThreadLocalCyclicDependencyGuardTestCase {

    private final Runnable[] runner = new Runnable[3];

    class ThreadLocalRunner implements Runnable {
        public AbstractInjector.CyclicDependencyException exception;
        private final Blocker blocker;
        private final AbstractInjector.ThreadLocalCyclicDependencyGuard guard;

        public ThreadLocalRunner() {
            this.blocker = new Blocker();
            this.guard = new AbstractInjector.ThreadLocalCyclicDependencyGuard() {
                @Override
				public Object run(final Object instance) {
                    try {
                        blocker.block();
                    } catch (InterruptedException e) {
                    }
                    return null;
                }
            };
        }

        public void run() {
            try {
                guard.observe(ThreadLocalRunner.class,null);
            } catch (AbstractInjector.CyclicDependencyException e) {
                exception = e;
            }
        }
    }

    public class Blocker {
        public void block() throws InterruptedException {
            final Thread thread = Thread.currentThread();
            synchronized (thread) {
                thread.wait();
            }
        }
    }

    private void initTest(final Runnable[] runner) throws InterruptedException {

        Thread racer[] = new Thread[runner.length];
        for(int i = 0; i < racer.length; ++i) {
            racer[i] =  new Thread(runner[i]);
        }

        for (Thread aRacer : racer) {
            aRacer.start();
            Thread.sleep(200);
        }

        for (Thread aRacer : racer) {
            synchronized (aRacer) {
                aRacer.notify();
            }
        }

        for (Thread aRacer : racer) {
            aRacer.join();
        }
    }

    @Test public void testCyclicDependencyWithThreadSafeGuard() throws InterruptedException {
        for(int i = 0; i < runner.length; ++i) {
            runner[i] = new ThreadLocalRunner();
        }

        initTest(runner);

        for (Runnable aRunner : runner) {
            assertNull(((ThreadLocalRunner) aRunner).exception);
        }
    }

    @Test public void testCyclicDependencyException() {
        final AbstractInjector.CyclicDependencyException cdEx = new AbstractInjector.CyclicDependencyException(getClass());
        cdEx.push(String.class);
        final Class[] classes = cdEx.getDependencies();
        assertEquals(2, classes.length);
        assertSame(getClass(), classes[0]);
        assertSame(String.class, classes[1]);
        assertTrue(cdEx.getMessage().indexOf(getClass().getName()) >= 0);
    }



}
