/*
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store.stores;

import java.util.HashMap;
import java.util.Map;

import org.picocontainer.logging.Logger;
import org.picocontainer.logging.store.LoggerNotFoundException;
import org.picocontainer.logging.store.LoggerStore;

/**
 * AbstractLoggerStore is an abstract implementation of LoggerStore for the
 * functionality common to all Loggers.
 * 
 * @author Mauro Talevi
 */
public abstract class AbstractLoggerStore implements LoggerStore {
    /** Map of Loggers held in the store */
    private final Map<String, Logger> loggers = new HashMap<String, Logger>();

    /** The Logger used by LogEnabled. */
    private Logger logger;

    /** The root Logger */
    private Logger rootLogger;

    /**
     * Provide a logger.
     * 
     * @param logger the logger
     */
    public void enableLogging(final Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (this.logger != null && this.logger.isDebugEnabled()) {
            final String message = "Root Logger returned";
            this.logger.debug(message);
        }
        final Logger logger = getRootLogger();
        if (logger == null) {
            final String message = "Root Logger is not defined";
            throw new LoggerNotFoundException(message);
        }
        return logger;
    }

    public Logger getLogger(final String name) {
        if (null == name) {
            throw new NullPointerException("name");
        }
        Logger logger = retrieveLogger(name);
        if (logger == null) {
            if (this.logger != null && this.logger.isDebugEnabled()) {
                final String message = "Logger named '" + name + "' not defined in configuration. New Logger "
                        + "created and returned.";
                this.logger.debug(message);
            }
            logger = createLogger(name);
            final Logger logger1 = logger;
            this.loggers.put(name, logger1);
        }
        return logger;
    }

    /**
     * Creates new Logger for the given category. This is logger-implementation
     * specific and will be implemented in concrete subclasses.
     */
    protected abstract Logger createLogger(String name);

    /**
     * Sets the root Logger.
     */
    protected final void setRootLogger(final Logger rootLogger) {
        this.rootLogger = rootLogger;
    }

    /**
     * Returns the root logger.
     * 
     * @return the root logger.
     */
    protected final Logger getRootLogger() {
        return this.rootLogger;
    }

    /**
     * Retrieve Logger from store map.
     * 
     * @param name the name of the Logger
     * @return the Logger instance or <code>null</code> if not found in map.
     */
    private Logger retrieveLogger(final String name) {
        Logger logger = (Logger) this.loggers.get(name);
        if (null != logger) {
            if (null != this.logger && this.logger.isDebugEnabled()) {
                final String message = "Retrieved Logger named: " + name;
                this.logger.debug(message);
            }
        }

        return logger;
    }
}
