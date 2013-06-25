/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/

package com.picocontainer.persistence.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.hibernate.EmptyInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoBuilder;
import com.picocontainer.parameters.ConstantParameter;

public class ScopedSessionTestCase {

    @Test
    public void canCreateRequestContainerWithItsOwnLifecycle() throws Exception {
        MutablePicoContainer container = new PicoBuilder().withLifecycle().withCaching().build();
        container.addComponent(Configuration.class, ConstructableConfiguration.class, new ConstantParameter(
                "/hibernate.cfg.xml"));
        container.addComponent(SessionFactory.class, ConfigurableSessionFactory.class);
        container.start();

        MutablePicoContainer requestContainer = container.makeChildContainer();
        // Normally you would use ThreadLocal Storage with this component.
        requestContainer.addComponent(Session.class, ScopedSession.class);
        requestContainer.start();

        Session session = requestContainer.getComponent(Session.class);
        assertNotNull(session);
        assertTrue(session instanceof ScopedSession);
        ScopedSession scopedSession = (ScopedSession) session;

        Session delegate = scopedSession.getDelegate();
        assertNotNull(delegate);

        assertSame("Repeated simple calls to getDelegate() should return the same Hibernate session", delegate,
                scopedSession.getDelegate());

        requestContainer.stop();
        requestContainer.start();

        assertNotSame("getDelegate() after request container is stopped should not be the same.", delegate,
                scopedSession.getDelegate());
        assertFalse("After container stop, hibernate sessions should be disconnected", delegate.isConnected());

        requestContainer.stop();
        requestContainer.dispose();
        container.removeChildContainer(requestContainer);

        try {
            scopedSession.getDelegate();
            fail("Delegate session should not allow interactions after dispose()");
        } catch (Exception ex) {
            // a-ok
            assertNotNull(ex.getMessage());
        }

        container.stop();
        container.dispose();
    }

    @Test
    public void canCreateAndDisposeSession() throws Exception {
        SessionFactory factory = (new ConstructableConfiguration("/hibernate.cfg.xml")).buildSessionFactory();
        ScopedSession delegator = new ScopedSession(factory);
        Session session = delegator.getDelegate();
        assertNotNull(session);

        assertSame(session, delegator.getDelegate());

        // test that closing invalidates session
        delegator.close();

        assertNotSame(session, delegator.getDelegate());
        session = delegator.getDelegate();

        // produce error
        try {
            assertNotNull(delegator.save(new Pojo()));
            fail("did not bombed on hibernate error");
        } catch (HibernateException e) {
            // that's ok
            assertNotNull(e.getMessage());
        }

        assertNotSame(session, delegator.getDelegate());
    }

    @Test
    public void canSavePojo() {
        Pojo pojo = new Pojo();
        pojo.setFoo("This is a test");
        SessionFactory factory = (new ConstructableConfiguration("/hibernate.cfg.xml")).buildSessionFactory();
        ScopedSession session = new ScopedSession(factory);

        try {
            Integer result = (Integer) session.save(pojo);
            assertNotNull(result);

            // Normally Close will force a new session to be created in the
            // background the next time it is used.
            session.close();

            // We're just doing it this way to show that it works with normal
            // hibenate procedures.
            session = new ScopedSession(factory);

            Fooable pojo2 = (Fooable) session.load(pojo.getClass(), result);
            assertNotNull(pojo);
            assertEquals(pojo.getId(), pojo2.getId());
            assertEquals(pojo.getFoo(), pojo2.getFoo());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
            factory.close();
        }
    }

    @Test
    public void canInvokeStringAndHashcodeOnScopedSession() {
        SessionFactory factory = (new ConstructableConfiguration("/hibernate.cfg.xml")).buildSessionFactory();
        ScopedSession session = new ScopedSession(factory);
        int beforeCreationHashCode = session.hashCode();
        assertTrue(beforeCreationHashCode > 0);
        assertNotNull(session.toString());

        // Force lazy creation of a session.
        Session delegate = session.getDelegate();
        assertNotNull(delegate);

        // If this is failing, we're getting too many collisions! Rework null
        // hashcode handling.
        assertNotSame(beforeCreationHashCode, session.hashCode());
        assertNotNull(session.toString());
    }

    @Test
    public void cannotSetInterceptorAfterConnectionRetrieved() {
        SessionFactory factory = (new ConstructableConfiguration("/hibernate.cfg.xml")).buildSessionFactory();
        ScopedSession session = new ScopedSession(factory);
        session.setInterceptor(EmptyInterceptor.INSTANCE);
        assertEquals(EmptyInterceptor.INSTANCE, session.getInterceptor());

        // Force creation of delegate interceptor.
        session.getDelegate();

        try {
            session.setInterceptor(EmptyInterceptor.INSTANCE);
        } catch (IllegalStateException e) {
            // A-ok
            assertNotNull(e.getMessage());
        }

    }
}
