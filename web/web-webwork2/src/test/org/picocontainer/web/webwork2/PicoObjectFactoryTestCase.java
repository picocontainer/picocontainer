/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.web.webwork2;

import static org.junit.Assert.fail;
import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import javax.servlet.http.HttpServletRequest;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoCompositionException;

/**
 * @author Mauro Talevi
 * @author Konstantin Pribluda
 */
@RunWith(JMock.class)
public final class PicoObjectFactoryTestCase {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    private PicoObjectFactory factory;
    private DefaultPicoContainer container;
    private final HttpServletRequest request = mockery.mock(HttpServletRequest.class);
    
    @Before public void setUp() {
        container = (DefaultPicoContainer)new DefaultPicoContainer().change(Characteristics.CACHE);
        factory = new PicoObjectFactory();
    }
    


    @Test public void testActionInstantiationWithInvalidClassName() throws Exception {
        try {
            factory.buildBean("invalidAction");
            fail("PicoCompositionException expected");
        } catch (PicoCompositionException e) {
            // expected
        }
    }




}