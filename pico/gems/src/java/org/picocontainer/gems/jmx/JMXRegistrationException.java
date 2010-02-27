/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Michael Ward                                    		 *
 *****************************************************************************/

package org.picocontainer.gems.jmx;

import org.picocontainer.PicoCompositionException;


/**
 * A registration exception caused trying to register the component with JMX.
 * @author Michael Ward
 */
@SuppressWarnings("serial")
public class JMXRegistrationException extends PicoCompositionException {



	/**
     * Construct a JMXRegistrationException with a particular message.
     * @param message the description of the exception
     */
    public JMXRegistrationException(final String message) {
        super(message);
    }

    /**
     * Construct a JMXRegistrationException with a causing {@link Throwable}.
     * @param cause the cause
     */
    public JMXRegistrationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Construct a JMXRegistrationException with a causing {@link Throwable} and a particular message.
     * @param message the description of the exception
     * @param cause the cause
     */
    public JMXRegistrationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
