/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer;

/**
 * Superclass for all Exceptions in PicoContainer. You can use this if you want to catch all exceptions thrown by
 * PicoContainer. Be aware that some parts of the PicoContainer API will also throw {@link NullPointerException} when
 * <code>null</code> values are provided for method arguments, and this is not allowed.
 * 
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public abstract class PicoException extends RuntimeException {

    /**
     * Construct a new exception with no cause and no detail message. Note modern JVMs may still track the exception
     * that caused this one.
     */
    protected PicoException() {
    }

    /**
     * Construct a new exception with no cause and the specified detail message.  Note modern JVMs may still track the
     * exception that caused this one.
     *
     * @param message the message detailing the exception.
     */
    protected PicoException(final String message) {
        super(message);
    }

    /**
     * Construct a new exception with the specified cause and no detail message.
     * 
     * @param cause the exception that caused this one.
     */
    protected PicoException(final Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new exception with the specified cause and the specified detail message.
     *
     * @param message the message detailing the exception.
     * @param cause   the exception that caused this one.
     */
    protected PicoException(final String message, final Throwable cause) {
        super(message,cause);
    }

}
