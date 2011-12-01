/*
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.junit.Test;
import org.picocontainer.logging.loggers.ConsoleLogger;
import org.picocontainer.logging.store.factories.ConsoleLoggerStoreFactory;
import org.picocontainer.logging.store.factories.InitialLoggerStoreFactory;
import org.picocontainer.logging.store.factories.JdkLoggerStoreFactory;
import org.picocontainer.logging.store.factories.PropertyLog4JLoggerStoreFactory;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Mauro Talevi
 * @author Peter Donald
 */
public class LoggerStoreFactoryTest extends AbstractTest {

    private static final String JDK_FILENAME = "jdk";
    private static final String LOGGING_PROPERTIES = "org/picocontainer/logging/store/logging.properties";
    private static final String LOG4J_PROPERTIES_FILENAME = "log4j-properties";
    private static final String LOG4J_PROPERTIES = "org/picocontainer/logging/store/log4j.properties";

    // InitialLoggerStoreFactory tests
    @Test(expected = LoggerStoreCreationException.class)
    public void testInitialLoggerStoreFactoryUsingDefaults() {
        final HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(ClassLoader.class.getName(), ClassLoader.getSystemClassLoader().getParent());
        final InitialLoggerStoreFactory factory = new InitialLoggerStoreFactory();
        LoggerStore foo = factory.createLoggerStore(config);
        assertNotNull(foo);
    }

    @Test
    public void testInitialLoggerStoreFactoryUsingSpecifiedType() {
        final HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(InitialLoggerStoreFactory.INITIAL_FACTORY, ConsoleLoggerStoreFactory.class.getName());
        final InitialLoggerStoreFactory factory = new InitialLoggerStoreFactory();
        final LoggerStore store = factory.createLoggerStore(config);
        runConsoleLoggerTest(store, ConsoleLogger.LEVEL_DEBUG);
    }

    @Test(expected = LoggerStoreCreationException.class)
    public void testInitialLoggerStoreFactoryWithInvalidType() {
        final HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(InitialLoggerStoreFactory.INITIAL_FACTORY, "Blah");
        final InitialLoggerStoreFactory factory = new InitialLoggerStoreFactory();
        LoggerStore foo = factory.createLoggerStore(config);
        assertNotNull(foo);
    }

    @Test 
    public void testInitialLoggerStoreFactoryFromConfigurerClassLoader() throws IOException {
        final HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(InitialLoggerStoreFactory.INITIAL_FACTORY, PropertyLog4JLoggerStoreFactory.class.getName());
        config.put(InputStream.class.getName(), getInputStream(LOG4J_PROPERTIES));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        runFactoryTest(new InitialLoggerStoreFactory(), config, LOG4J_PROPERTIES_FILENAME, ConsoleLogger.LEVEL_DEBUG);
        Thread.currentThread().setContextClassLoader(classLoader);
        // asserts ?
    }

    @Test
    public void testInitialLoggerStoreFactoryFromSpecifiedClassLoader() throws IOException {
        final HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(InitialLoggerStoreFactory.INITIAL_FACTORY, PropertyLog4JLoggerStoreFactory.class.getName());
        config.put(InputStream.class.getName(), getInputStream(LOG4J_PROPERTIES));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        config.put(ClassLoader.class.getName(), classLoader);
        runFactoryTest(new InitialLoggerStoreFactory(), config, LOG4J_PROPERTIES_FILENAME, ConsoleLogger.LEVEL_DEBUG);
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Test
    public void testInitialLoggerStoreFactoryFromContextClassLoader() throws IOException {
        Thread.currentThread().setContextClassLoader(InitialLoggerStoreFactory.class.getClassLoader());
        final HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(InitialLoggerStoreFactory.INITIAL_FACTORY, PropertyLog4JLoggerStoreFactory.class.getName());
        config.put(InputStream.class.getName(), getInputStream(LOG4J_PROPERTIES));
        runFactoryTest(new InitialLoggerStoreFactory(), config, LOG4J_PROPERTIES_FILENAME, ConsoleLogger.LEVEL_DEBUG);
    }

    // JdkLoggerStoreFactory tests
    @Test
    public void testJdkLoggerStoreFactoryInvalidInput() throws Exception {
        createLoggerStoreWithEmptyConfiguration(new JdkLoggerStoreFactory());
    }

    @Test
    public void testJdkLoggerStoreFactoryWithProperties() throws IOException {
        final Properties properties = new Properties();
        properties.load(getInputStream(LOGGING_PROPERTIES));
        final HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(Properties.class.getName(), properties);
        runFactoryTest(new JdkLoggerStoreFactory(), config, JDK_FILENAME, ConsoleLogger.LEVEL_DEBUG);
    }

    @Test
    public void testJdkLoggerStoreFactoryWithStreams() throws IOException {
        runStreamBasedFactoryTest(LOGGING_PROPERTIES, new JdkLoggerStoreFactory(), JDK_FILENAME,
                new HashMap<String, Object>(), ConsoleLogger.LEVEL_DEBUG);
    }

}
