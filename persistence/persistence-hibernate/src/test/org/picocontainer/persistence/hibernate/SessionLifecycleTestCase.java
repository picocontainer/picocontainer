/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package org.picocontainer.persistence.hibernate;

import static org.picocontainer.tck.MockFactory.mockeryWithCountingNamingScheme;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

/**
 * Test that lifecycle closes session factory
 */
public class SessionLifecycleTestCase {

	private Mockery mockery = mockeryWithCountingNamingScheme();
	
    @Test 
    public void canCloseSessionOnStop() throws Exception {
    	final Session session = mockery.mock(Session.class);
    	mockery.checking(new Expectations(){{
            one(session).flush();
    		one(session).close();
    	}});
        SessionLifecycle lifecycle = new SessionLifecycle(session);
        lifecycle.stop();
    }
    
    @Test(expected=HibernateException.class)
    public void cannotCloseSessionOnStop() throws Exception {
        final Session session = mockery.mock(Session.class);
        mockery.checking(new Expectations(){{
            one(session).flush();
            will(throwException(new HibernateException("mock")));
        }});
        SessionLifecycle lifecycle = new SessionLifecycle(session);
        lifecycle.stop();
    }

}
