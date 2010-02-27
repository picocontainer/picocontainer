/*
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store;

import org.picocontainer.logging.Logger;

/**
 * <p>
 * LoggerStore represents the logging hierarchy for a Logger, as defined by its
 * configuration.
 * </p>
 * <p>
 * The LoggerStore has an associated LoggerStoreFactory which also acts as a
 * configurator for the Logger.
 * </p>
 * <p>
 * Whenever an application has finished using the LoggerStore it will call the
 * close() method indicating that the logger hierarchy should also be shutdown.
 * </p>
 * 
 * @author Mauro Talevi
 * @author Peter Donald
 */
public interface LoggerStore {
    /**
     * Retrieves the root Logger from the store.
     * 
     * @return the Logger
     * @throws LoggerNotFoundException if unable to retrieve Logger
     */
    Logger getLogger();

    /**
     * Retrieves a Logger hierarchy from the store for a given category name.
     * 
     * @param categoryName the name of the logger category.
     * @return the Logger
     * @throws LoggerNotFoundException if unable to retrieve Logger
     */
    Logger getLogger(String categoryName);

    /**
     * Closes the LoggerStore and shuts down the logger hierarchy.
     */
    void close();
}
