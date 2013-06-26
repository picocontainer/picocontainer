/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * license.txt file.
 ******************************************************************************/

package com.picocontainer.persistence.hibernate;

import org.hibernate.SessionFactory;

import com.picocontainer.Startable;

/**
 * Add lifecycle methods to the delegate factory
 * 
 * @author Jose Peleteiro
 * @author Mauro Talevi
 */
public final class SessionFactoryLifecycle implements Startable {

    private final SessionFactory sessionFactory;

    public SessionFactoryLifecycle(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void start() {
    }

    public void stop() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
