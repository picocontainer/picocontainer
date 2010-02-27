/*
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store;

import static org.picocontainer.logging.store.LoggerStoreFactory.FILE_LOCATION;
import static org.picocontainer.logging.store.factories.InitialLoggerStoreFactory.INITIAL_FACTORY;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.picocontainer.logging.store.factories.DOMLog4JLoggerStoreFactory;
import org.picocontainer.logging.store.factories.InitialLoggerStoreFactory;
import org.picocontainer.logging.store.factories.JdkLoggerStoreFactory;
import org.picocontainer.logging.store.factories.PropertyLog4JLoggerStoreFactory;

/**
 * Configurator is a collection of utility methods to create and configure
 * LoggerStore objects of different types using configuration resources. Log4J
 * and JDK Loggers are supported. In the case of Log4J, both DOM and Property
 * configuration types are supported.
 * 
 * @author Mauro Talevi
 */
public class Configurator {
    /** Constant used to define Log4J type with DOMConfigurator */
    public static final String LOG4J_DOM = "log4j-dom";

    /** Constant used to define Log4J type with PropertyConfigurator */
    public static final String LOG4J_PROPERTY = "log4j-property";

    /** Constant used to define JDK type */
    public static final String JDK = "jdk";

    /**
     * Create and configure a {@link LoggerStore} from a specified configuration
     * resource.
     * 
     * @param configuratorType the type of the configurator
     * @param resource the String encoding the path of the configuration
     *            resource
     * @return the configured LoggerStore
     * @throws LoggerStoreCreationException if unable to create the LoggerStore
     */
    public static LoggerStore createLoggerStore(final String configuratorType, final String resource) {
        final InitialLoggerStoreFactory factory = new InitialLoggerStoreFactory();
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put(INITIAL_FACTORY, getFactoryClassName(configuratorType));
        data.put(FILE_LOCATION, resource);
        return factory.createLoggerStore(data);
    }

    /**
     * Create and configure a {@link LoggerStore} from a specified configuration
     * resource.
     * 
     * @param configuratorType the type of the configurator
     * @param resource the InputStream of the configuration resource
     * @return the configured LoggerStore
     * @throws LoggerStoreCreationException if unable to create the LoggerStore
     */
    public static LoggerStore createLoggerStore(final String configuratorType, final InputStream resource) {
        final InitialLoggerStoreFactory factory = new InitialLoggerStoreFactory();
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put(INITIAL_FACTORY, getFactoryClassName(configuratorType));
        data.put(InputStream.class.getName(), resource);
        return factory.createLoggerStore(data);
    }

    /**
     * Get the Factory class name of the LoggerStoreFactory that corresponds to
     * specified type of Logger.
     * 
     * @param type the type of Configurator
     * @throws LoggerStoreCreationException if type not known
     */
    private static String getFactoryClassName(final String type) {
        if (LOG4J_DOM.equals(type)) {
            return DOMLog4JLoggerStoreFactory.class.getName();
        } else if (LOG4J_PROPERTY.equals(type)) {
            return PropertyLog4JLoggerStoreFactory.class.getName();
        } else if (JDK.equals(type)) {
            return JdkLoggerStoreFactory.class.getName();
        } else {
            final String message = "Unknown type " + type;
            throw new LoggerStoreCreationException(message);
        }
    }
}
