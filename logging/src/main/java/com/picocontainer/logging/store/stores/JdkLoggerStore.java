/*
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.picocontainer.logging.store.stores;

import java.io.InputStream;
import java.util.logging.LogManager;

import com.picocontainer.logging.Logger;
import com.picocontainer.logging.loggers.JdkLogger;
import com.picocontainer.logging.store.LoggerStoreCreationException;

/**
 * JdkLoggerStore extends AbstractLoggerStore to provide the implementation
 * specific to the JDK logger.
 * 
 * @author Mauro Talevi
 */
public class JdkLoggerStore extends AbstractLoggerStore {
    /** The LogManager repository */
    private final LogManager manager;

    /**
     * Creates a <code>JdkLoggerStore</code> using the configuration resource.
     * 
     * @param resource the InputStream encoding the configuration resource
     * @throws LoggerStoreCreationException if fails to create store or
     *             configure Logger
     */
    public JdkLoggerStore(final InputStream resource) {
        try {
            this.manager = LogManager.getLogManager();
            this.manager.readConfiguration(resource);
            setRootLogger(new JdkLogger(java.util.logging.Logger.getLogger("global")));
        } catch (Exception e) {
            final String message = "Failed to create logger store for resource " + resource;
            throw new LoggerStoreCreationException(message, e);
        }
    }

    /**
     * Creates new JdkLogger for the given category.
     */
    protected Logger createLogger(final String name) {
        return new JdkLogger(java.util.logging.Logger.getLogger(name));
    }

    /**
     * Closes the LoggerStore and shuts down the logger hierarchy.
     */
    public void close() {
        this.manager.reset();
    }
}
