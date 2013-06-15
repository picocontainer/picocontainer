/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.defaults.issues;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Synchronizing;
import org.picocontainer.injectors.ConstructorInjection;

public final class Issue0199TestCase {

    public static class A {
        public A(final C c) {}
    }

    public static class B {
        public B(final C c) {}
    }

    public static class C {}

    final class Runner extends Thread {
        private final PicoContainer container;
        private final Object key;
        private Throwable throwable;
        private boolean finished;

        Runner(final String name, final PicoContainer container, final Object key) {
            super(name);
            this.container = container;
            this.key = key;
        }

        @Override
		public void run() {
            try {
                report("Started instantiating " + key.toString());
                container.getComponent(key);
                report("Finished instantiating " + key.toString());
                finished = true;
            } catch (Throwable t) {
                this.throwable = t;
            }
        }

        private void report(final String messsage) {
            System.out.println(getName() + ": " + messsage);
        }

        public boolean isFinished() {
            return finished;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

    @Test public void testPicoContainerCausesDeadlock() throws InterruptedException {
        DefaultPicoContainer container = createContainer();
        container.addComponent("A", A.class);
        container.addComponent("B", B.class);
        container.addComponent("C", C.class);

        final int THREAD_COUNT = 2;
        List runnerList = new ArrayList(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; ++i) {
            Runner runner = new Runner("Runner " + i, container, (i % 2 == 0) ? "A" : "B");
            runnerList.add(runner);
            runner.start();
        }

        final long WAIT_TIME = 1000;

        for (int i = 0; i < THREAD_COUNT; ++i) {
            Runner runner = (Runner) runnerList.get(i);
            runner.join(WAIT_TIME);
            assertTrue("Deadlock occurred", runner.isFinished());
        }
    }

    private DefaultPicoContainer createContainer() {
        return new DefaultPicoContainer(
                new Synchronizing().wrap(new ConstructorInjection()));
    }
}
