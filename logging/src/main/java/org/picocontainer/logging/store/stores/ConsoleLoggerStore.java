/*
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store.stores;

import org.picocontainer.logging.Logger;
import org.picocontainer.logging.loggers.ConsoleLogger;
import org.picocontainer.logging.store.LoggerStoreCreationException;

/**
 * ConsoleLoggerStore extends AbstractLoggerStore to provide the implementation
 * specific that just writes to console.
 * 
 * @author Mauro Talevi
 */
public class ConsoleLoggerStore extends AbstractLoggerStore {
    /**
     * Creates a <code>ConsoleLoggerStore</code> using the specified Logger
     * level.
     * 
     * @param level the debug level of ConsoleLoggerStore
     * @throws LoggerStoreCreationException if fails to create or configure
     *             Logger
     */
    public ConsoleLoggerStore(final int level) {
        setRootLogger(new ConsoleLogger(level));
    }

    /**
     * Creates new ConsoleLogger for the given category.
     */
    protected Logger createLogger(final String name) {
        return getRootLogger().getChildLogger(name);
    }

    /**
     * Closes the LoggerStore and shuts down the logger hierarchy.
     */
    public void close() {
    }
}
