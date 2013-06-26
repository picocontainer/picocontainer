/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * license.txt file.
 ******************************************************************************/

package com.picocontainer.persistence.hibernate;

import org.hibernate.Session;

import com.picocontainer.Startable;

/**
 * Adds lifecycle method to the delegate session.
 * 
 * @author Jose Peleteiro
 */
public final class SessionLifecycle implements Startable {

    private final Session session;

    public SessionLifecycle(Session session) {
        this.session = session;
    }

    public void start() {
    }

    public void stop() {
        if (session != null) {
            session.flush();
            session.close();
        }
    }

}
