/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/
package org.picocontainer.gems.adapters;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.ObjectReference;
import org.picocontainer.references.ThreadLocalReference;


/**
 * Unit test for ThreadLocalReference
 *
 * @author J&ouml;rg Schaible
 */
public final class ThreadLocalReferenceTest {

    private List m_exceptionList;

    @Before
    public void setUp() throws Exception {
        m_exceptionList = Collections.synchronizedList(new ArrayList());
    }

    final class RunIt implements Runnable {

        private final ObjectReference m_reference;

        /**
         * Construct an instance.
         *
         * @param reference
         */
        public RunIt(final ObjectReference reference) {
            super();
            m_reference = reference;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                final Thread thread = Thread.currentThread();
                m_reference.set(thread.getName());
                synchronized (thread) {
                    thread.wait();
                }
                assertEquals(thread.getName(), m_reference.get());
            } catch (InterruptedException e) {
                m_exceptionList.add(e);
            }
        }
    }

    /**
     * Test working ThreadLocalReference
     *
     * @throws InterruptedException
     */
    @Test public void testThreadLocalReference() throws InterruptedException {
        final ThreadLocalReference reference = new ThreadLocalReference();
        final Thread[] threads = new Thread[]{
                new Thread(new RunIt(reference), "junit-TLR-1"), new Thread(new RunIt(reference), "junit-TLR-2"),
                new Thread(new RunIt(reference), "junit-TLR-3")};
        reference.set("Hello");
        for (Thread thread1 : threads) {
            thread1.start();
        }
        Thread.sleep(50);
        assertEquals("Hello", reference.get());
        for (Thread thread : threads) {
            synchronized (thread) {
                thread.notify();
            }
        }
        Thread.sleep(50);
        assertEquals("Unexpected Exceptions: " + m_exceptionList, 0, m_exceptionList.size());
    }
}
