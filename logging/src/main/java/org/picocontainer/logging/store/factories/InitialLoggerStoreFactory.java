/*
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store.factories;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.picocontainer.logging.store.LoggerStore;
import org.picocontainer.logging.store.LoggerStoreCreationException;
import org.picocontainer.logging.store.LoggerStoreFactory;

/**
 * This is the initial LoggerStoreFactory tyhat the user accesses to create
 * their LoggerStore when the type is configurable.
 * 
 * @author Peter Donald
 * @author Mauro Talevi
 */
public class InitialLoggerStoreFactory implements LoggerStoreFactory {
    /**
     * The INITIAL_FACTORY key. Used to define the classname of the initial
     * LoggerStoreFactory. If not specified will attempt to use the
     * ConsoleLoggerStoreFactory.
     */
    public static final String INITIAL_FACTORY = "org.picocontainer.logging.store.factory";

    /**
     * The name of properties file loaded from ClassLoader. This property file
     * will be used to load default configuration settings if user failed to
     * specify them.
     */
    public static final String DEFAULT_PROPERTIES = "META-INF/picocontainer/loggerstore.properties";

    /**
     * Create LoggerStore by first determining the correct LoggerStoreFactory to
     * use and then delegating to that factory. See Class Javadocs for the
     * process of locating LoggerStore.
     * 
     * @param config the input configuration
     * @return the LoggerStore
     * @throws LoggerStoreCreationException if unable to create the LoggerStore
     *             for any reason.
     */
    public LoggerStore createLoggerStore(final Map<String, Object> config) {
        final ClassLoader classLoader = getClassLoader(config);

        String type = (String) config.get(INITIAL_FACTORY);
        Map<String, Object> data = config;
        if (null == type) {
            data = loadDefaultConfig(data, classLoader);
            type = (String) data.get(INITIAL_FACTORY);
        }
        final LoggerStoreFactory factory = createLoggerStoreFactory(type, classLoader);
        return factory.createLoggerStore(data);
    }

    /**
     * Retrieve the classloader from data map. If no classloader is specified
     * then use ContextClassLoader. If ContextClassLoader not specified then use
     * ClassLoader that loaded this class.
     * 
     * @param data the configuration data
     * @return a ClassLoader
     */
    private ClassLoader getClassLoader(final Map<String, Object> data) {
        ClassLoader loader = (ClassLoader) data.get(ClassLoader.class.getName());
        if (null == loader) {
            loader = Thread.currentThread().getContextClassLoader();
            if (null == loader) {
                loader = InitialLoggerStoreFactory.class.getClassLoader();
            }
        }
        return loader;
    }

    /**
     * Load the default properties for LoggerStoreFactory.
     * 
     * @param initial the input data
     * @param classLoader the classLoader to load properties files from
     * @return the new configuration data
     * @throws LoggerStoreCreationException if unable to load properties
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadDefaultConfig(final Map<String, Object> initial, final ClassLoader classLoader) {
        try {
            final HashMap<String, Object> map = new HashMap<String, Object>();
            final Enumeration<URL> resources = classLoader.getResources(DEFAULT_PROPERTIES);
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                final InputStream stream = url.openStream();
                final Properties properties = new Properties();
                properties.load(stream);
                for (Enumeration e = properties.keys(); e.hasMoreElements();) {
                    String key = e.nextElement().toString();
                    map.put(key, properties.getProperty(key));
                }
            }
            map.putAll(initial);
            return map;
        } catch (Exception e) {
            final String message = "Failed to load initial configuration " + initial;
            throw new LoggerStoreCreationException(message, e);
        }
    }

    /**
     * Create a {@link LoggerStoreFactory} for specified loggerType.
     * 
     * @param type the type of the Logger to use.
     * @return the created {@link LoggerStoreFactory}
     */
    private LoggerStoreFactory createLoggerStoreFactory(final String type, final ClassLoader classLoader) {
        if (null == type) {
            final String message = "No LoggerStoreFactory type specified.";
            throw new LoggerStoreCreationException(message);
        }

        try {
            return (LoggerStoreFactory) classLoader.loadClass(type).newInstance();
        } catch (final Exception e) {
            final String message = "Failed to created LoggerStoreFactory " + type;
            throw new LoggerStoreCreationException(message);
        }
    }
}
