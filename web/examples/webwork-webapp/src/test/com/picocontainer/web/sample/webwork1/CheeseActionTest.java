/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web.sample.webwork1;

import junit.framework.TestCase;

import com.picocontainer.web.sample.webwork1.CheeseAction;
/**
 * Simple test case to demonstrate webwork1 action testing
 * @author Konstantin Pribluda
 *
 */
// SNIPPET START: testcase
public class CheeseActionTest extends TestCase {
    
    /**
    * test that cheese action works and store cheese in service
    */
    public void testCheeseAction() throws Exception {
        DefaultCheeseService service = new DefaultCheeseService(new InMemoryCheeseDao());
        
        CheeseAction action = new CheeseAction(service);
        action.getCheese().setName("gouda");
        action.getCheese().setCountry("Netherlands");
        action.setCommand("save");
        assertEquals(CheeseAction.SUCCESS,action.execute());

        // Cheddar + 3 others exists already -- count 4 already.

        assertEquals(5, service.getCheeses().size());
        
        action.setCommand("remove");
        assertEquals(CheeseAction.SUCCESS,action.execute());
        
        assertEquals(4, service.getCheeses().size());
        
    }
    public void testFoo() {

    }
}
// SNIPPET END: testcase

