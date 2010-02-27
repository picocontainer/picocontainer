/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.remoting;

import org.picocontainer.PicoCompositionException;
import org.picocontainer.web.PicoContainerWebException;

public class NullPicoWebRemotingMonitor implements PicoWebRemotingMonitor {

    public Object picoCompositionExceptionForMethodInvocation(PicoCompositionException e) {
        return new ErrorReply(e.getMessage());
    }

    public Object runtimeExceptionForMethodInvocation(RuntimeException e) {
        Class<? extends RuntimeException> appBaseRuntimeException = getAppBaseRuntimeException();
        if (appBaseRuntimeException != null && appBaseRuntimeException.isAssignableFrom(e.getClass()) ||
            PicoContainerWebException.class.isAssignableFrom(e.getClass()) ) {
            return new ErrorReply(e.getMessage());
        } else {
            return otherRuntimeException(e);
        }
    }

    protected Object otherRuntimeException(RuntimeException e) {
        throw e;
    }

    public Object nullParameterForMethodInvocation(String parameterName) {
        return new ErrorReply("Parameter '" + parameterName + "' missing");
    }

    protected Class<? extends RuntimeException>getAppBaseRuntimeException() {
        return null;
    }

}
