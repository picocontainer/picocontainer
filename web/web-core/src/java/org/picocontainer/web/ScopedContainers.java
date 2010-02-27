/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Storing;

public final class ScopedContainers {

    private final MutablePicoContainer applicationContainer;
    private final MutablePicoContainer sessionContainer;
    private final MutablePicoContainer requestContainer;
    private final Storing sessionStoring;
    private final Storing requestStoring;
    private final ThreadLocalLifecycleState sessionState;
    private final ThreadLocalLifecycleState requestState;

    public ScopedContainers(MutablePicoContainer applicationContainer, MutablePicoContainer sessionContainer, MutablePicoContainer requestContainer, Storing sessionStoring, Storing requestStoring, ThreadLocalLifecycleState sessionState, ThreadLocalLifecycleState requestState) {
        this.applicationContainer = applicationContainer;
        this.sessionContainer = sessionContainer;
        this.requestContainer = requestContainer;
        this.sessionStoring = sessionStoring;
        this.requestStoring = requestStoring;
        this.sessionState = sessionState;
        this.requestState = requestState;
    }

    MutablePicoContainer getApplicationContainer() {
        return applicationContainer;
    }

    MutablePicoContainer getSessionContainer() {
        return sessionContainer;
    }

    MutablePicoContainer getRequestContainer() {
        return requestContainer;
    }

    Storing getSessionStoring() {
        return sessionStoring;
    }

    Storing getRequestStoring() {
        return requestStoring;

    }

    ThreadLocalLifecycleState getSessionState() {
        return sessionState;
    }

    ThreadLocalLifecycleState getRequestState() {
        return requestState;
    }
}
