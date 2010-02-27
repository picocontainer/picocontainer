/*
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store.stores;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;
import org.picocontainer.logging.Logger;
import org.picocontainer.logging.loggers.Log4JLogger;
import org.picocontainer.logging.store.LoggerStoreCreationException;
import org.w3c.dom.Element;

/**
 * Log4JLoggerStore extends AbstractLoggerStore to provide the implementation
 * specific to the Log4J logger.
 * 
 * @author Mauro Talevi
 */
public class Log4JLoggerStore extends AbstractLoggerStore {
    /** The logger repository */
    private final LoggerRepository repository;

    /**
     * Creates a <code>Log4JLoggerStore</code> using the configuration
     * resource
     * 
     * @param resource the Element encoding the configuration resource
     * @throws LoggerStoreCreationException if fails to create or configure
     *             Logger
     */
    public Log4JLoggerStore(final Element resource) {
        LogManager.resetConfiguration();
        this.repository = LogManager.getLoggerRepository();
        final DOMConfigurator configurator = new DOMConfigurator();
        configurator.doConfigure(resource, this.repository);
        setRootLogger(new Log4JLogger(this.repository.getRootLogger()));
    }

    /**
     * Creates a <code>Log4JLoggerStore</code> using the configuration
     * resource
     * 
     * @param resource the InputStream encoding the configuration resource
     * @throws LoggerStoreCreationException if fails to create or configure
     *             Logger
     */
    public Log4JLoggerStore(final InputStream resource) {
        LogManager.resetConfiguration();
        this.repository = LogManager.getLoggerRepository();
        final DOMConfigurator configurator = new DOMConfigurator();
        configurator.doConfigure(resource, this.repository);
        setRootLogger(new Log4JLogger(this.repository.getRootLogger()));
    }

    /**
     * Creates a <code>Log4JLoggerStore</code> using the configuration
     * resource
     * 
     * @param resource the Properties encoding the configuration resource
     * @throws LoggerStoreCreationException if fails to create or configure
     *             Logger
     */
    public Log4JLoggerStore(final Properties resource) {
        LogManager.resetConfiguration();
        this.repository = LogManager.getLoggerRepository();
        final PropertyConfigurator configurator = new PropertyConfigurator();
        configurator.doConfigure(resource, this.repository);
        setRootLogger(new Log4JLogger(this.repository.getRootLogger()));
    }

    /**
     * Creates new Log4JLogger for the given category.
     */
    protected Logger createLogger(final String categoryName) {
        return new Log4JLogger(this.repository.getLogger(categoryName));
    }

    /**
     * Closes the LoggerStore and shuts down the logger hierarchy.
     */
    public void close() {
        this.repository.shutdown();
    }
}
