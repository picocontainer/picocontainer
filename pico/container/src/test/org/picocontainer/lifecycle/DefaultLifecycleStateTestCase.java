/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package org.picocontainer.lifecycle;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

/**
 * @author Michael Rimov
 */
public class DefaultLifecycleStateTestCase {

    DefaultLifecycleState dls;

    @Before
    public void foo() {
        dls = new DefaultLifecycleState();
    }

    @Test public void testNormalLifecycle() {
        dls.starting();
        dls.stopping();
        dls.stopped();
        dls.disposing();
        dls.disposed();
	}

    @Test public void testReStartLifecycle() {
        dls.starting();
        dls.stopping();
        dls.stopped();
        dls.starting();
        dls.stopping();
        dls.stopped();
	}

    @Test public void testDisposalWithoutStarting() {
        dls.disposing();
        dls.disposed();
	}

    @Test public void testDisposalWithoutStop() {
        dls.starting();
        try {
            dls.disposing();
            Assert.fail("should have barfed");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().endsWith("STARTED"));
            //expected
        }
    }

    @Test public void testStopWithoutStart() {
        try {
            dls.stopping();
            Assert.fail("should have barfed");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().endsWith("CONSTRUCTED"));
            //expected
        }
    }

}
