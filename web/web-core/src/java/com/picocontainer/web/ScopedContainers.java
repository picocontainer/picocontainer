/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web;

import com.picocontainer.MutablePicoContainer;
import com.picocontainer.behaviors.Storing;
import com.picocontainer.security.PicoAccessPermission;
import com.picocontainer.security.SecurityWrappingPicoContainer;

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
    
    void dispose() {
    	if (requestStoring != null) {
    		requestStoring.dispose();
    	}

    	if (this.requestState != null) {
    		requestState.invalidateStateModelForThread();
    	}

    	if (sessionStoring != null) {
    		sessionStoring.dispose();
    	}
    	
    	if (this.sessionState != null) {
    		sessionState.invalidateStateModelForThread();
    	}
    	
    	
    }

	@Override
	protected void finalize() throws Throwable {
		try {
			dispose();
		} finally {
			super.finalize();
		}
	}
    
    
}
