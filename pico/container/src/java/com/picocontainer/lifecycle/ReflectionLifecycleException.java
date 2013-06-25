/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/
package com.picocontainer.lifecycle;

import com.picocontainer.PicoException;

/**
 * Subclass of {@link PicoException} that is thrown when there is a problem
 * invoking lifecycle methods via reflection.
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class ReflectionLifecycleException extends PicoException {


	/**
     * Construct a new exception with the specified cause and the specified detail message.
     *
     * @param message the message detailing the exception.
     * @param cause   the exception that caused this one.
     */
    protected ReflectionLifecycleException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
