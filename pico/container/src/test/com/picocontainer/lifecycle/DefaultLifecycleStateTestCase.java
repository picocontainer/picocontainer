/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package com.picocontainer.lifecycle;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.picocontainer.lifecycle.DefaultLifecycleState;

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
        dls.starting(null);
        dls.stopping(null);
        dls.stopped();
        dls.disposing(null);
        dls.disposed();
	}

    @Test public void testReStartLifecycle() {
        dls.starting(null);
        dls.stopping(null);
        dls.stopped();
        dls.starting(null);
        dls.stopping(null);
        dls.stopped();
	}

    @Test public void testDisposalWithoutStarting() {
        dls.disposing(null);
        dls.disposed();
	}

    @Test public void testDisposalWithoutStop() {
        dls.starting(null);
        try {
            dls.disposing("test");
            Assert.fail("should have barfed");
        } catch (IllegalStateException e) {
            //expected
        	assertTrue(e.getMessage().contains("test"));
            assertTrue(e.getMessage().endsWith("STARTED"));
        }
    }

    @Test public void testStopWithoutStart() {
        try {
            dls.stopping("test2");
            Assert.fail("should have barfed");
        } catch (IllegalStateException e) {
            //expected
            assertTrue(e.getMessage().endsWith("CONSTRUCTED"));
        	assertTrue(e.getMessage().contains("test2"));
        }
    }

}
