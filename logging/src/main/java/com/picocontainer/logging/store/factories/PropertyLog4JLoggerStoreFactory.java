/*
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.picocontainer.logging.store.factories;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import com.picocontainer.logging.store.LoggerStore;
import com.picocontainer.logging.store.LoggerStoreCreationException;
import com.picocontainer.logging.store.stores.Log4JLoggerStore;

/**
 * PropertyLog4JLoggerStoreFactory is an implementation of LoggerStoreFactory
 * for the Log4J Logger using a property configuration resource.
 * 
 * @author Mauro Talevi
 * @author Peter Donald
 */
public class PropertyLog4JLoggerStoreFactory extends AbstractLoggerStoreFactory {

    protected LoggerStore doCreateLoggerStore(final Map<String, Object> config) {
        try {
            final Properties properties = (Properties) config.get(Properties.class.getName());
            if (null != properties) {
                return new Log4JLoggerStore(properties);
            }
            final InputStream resource = getInputStream(config);
            if (null != resource) {
                return new Log4JLoggerStore(createPropertiesFromStream(resource));
            }
            return missingConfiguration();
        } catch (Exception e) {
            final String message = "Failed to create logger store for configuration " + config;
            throw new LoggerStoreCreationException(message, e);
        }
    }

    private Properties createPropertiesFromStream(final InputStream resource) throws IOException {
        final Properties properties = new Properties();
        properties.load(resource);
        return properties;
    }
}
