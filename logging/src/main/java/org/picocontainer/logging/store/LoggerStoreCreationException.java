/*
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store;

/**
 * Thrown when a logger store creation fails
 * 
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class LoggerStoreCreationException extends RuntimeException {

    public LoggerStoreCreationException(String message) {
        super(message);
    }

    public LoggerStoreCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}
