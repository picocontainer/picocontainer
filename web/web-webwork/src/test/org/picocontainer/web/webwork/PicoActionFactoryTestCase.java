/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.web.webwork;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.behaviors.Caching;

/**
 * @author Konstantin Pribluda
 * @author Mauro Talevi
 */
@RunWith(JMock.class)
public class PicoActionFactoryTestCase {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    private PicoActionFactory factory;
    private DefaultPicoContainer container;
    
    @Before public void setUp() {
        factory = new PicoActionFactory();
        container = new DefaultPicoContainer(new Caching());
    }
    

    @Test public void testActionInstantiationWhichFailsDueToFailedDependencies() throws Exception {
        TestAction action = (TestAction) factory
                .getActionImpl(TestAction.class.getName());
        assertNull(action);
    }

    @Test public void testActionInstantiationWithInvalidClassName() throws Exception {
        container.addComponent("foo");
        TestAction action = (TestAction) factory
                .getActionImpl("invalidAction");
        assertNull(action);
    }


    @Test public void testActionInstantiationWhichHasAlreadyBeenRequested() throws Exception {
        container.addComponent("foo");
        TestAction action1 = (TestAction) factory
                .getActionImpl(TestAction.class.getName());
        TestAction action2 = (TestAction) factory
                .getActionImpl(TestAction.class.getName());
        assertSame(action1, action2);
    }
    

}