/*
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store;

import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.picocontainer.logging.loggers.ConsoleLogger;
import org.picocontainer.logging.store.stores.ConsoleLoggerStore;
import org.picocontainer.logging.store.stores.JdkLoggerStore;
import org.picocontainer.logging.store.stores.Log4JLoggerStore;
import org.xml.sax.SAXException;

/**
 * @author Mauro Talevi
 * @author Peter Donald
 */
public class LoggerStoreTest extends AbstractTest {

    private static final String LOG4J_XML_FILENAME = "log4j-xml";
    private static final String LOG4J_XML = "org/picocontainer/logging/store/log4j.xml";
    private static final String LOG4J_PROPERTIES_FILENAME = "log4j-properties";
    private static final String LOG4J_PROPERTIES = "org/picocontainer/logging/store/log4j.properties";
    private static final String JDK_FILENAME = "jdk";
    private static final String LOGGING_PROPERTIES = "org/picocontainer/logging/store/logging.properties";

    @Test(expected = LoggerNotFoundException.class)
    public void testNullRootLogger() {
        final LoggerStore store = new MalformedLoggerStore();
        store.getLogger();
    }

    // ConsoleLoggerStore tests
    @Test
    public void testConsoleLoggerStore() {
        final LoggerStore store = new ConsoleLoggerStore(ConsoleLogger.LEVEL_DEBUG);
        runConsoleLoggerTest(store, ConsoleLogger.LEVEL_DEBUG);
    }

    @Test
    public void testConsoleLoggerStoreNoDebug() {
        final LoggerStore store = new ConsoleLoggerStore(ConsoleLogger.LEVEL_DEBUG);
        runConsoleLoggerTest(store, ConsoleLogger.LEVEL_NONE);
    }

    // Log4JLoggerStore tests
    @Test
    public void testLog4JElementConfiguration() throws IOException, ParserConfigurationException, SAXException {
        final LoggerStore store = new Log4JLoggerStore(buildElement(getInputStream(LOG4J_XML),
                new org.apache.log4j.xml.Log4jEntityResolver(), null));
        runLoggerTest(LOG4J_XML_FILENAME, store, ConsoleLogger.LEVEL_DEBUG);
    }

    @Test
    public void testLog4JElementConfigurationNoDebug() throws IOException, ParserConfigurationException, SAXException {
        final LoggerStore store = new Log4JLoggerStore(buildElement(getInputStream(LOG4J_XML),
                new org.apache.log4j.xml.Log4jEntityResolver(), null));
        runLoggerTest(LOG4J_XML_FILENAME, store, ConsoleLogger.LEVEL_NONE);
    }

    @Test
    public void testLog4JElementConfigurationNoLog() throws IOException, ParserConfigurationException, SAXException {
        final LoggerStore store = new Log4JLoggerStore(buildElement(getInputStream(LOG4J_XML),
                new org.apache.log4j.xml.Log4jEntityResolver(), null));
        runLoggerTest(LOG4J_XML_FILENAME, store);
    }

    @Test
    public void testLog4JInputStreamConfiguration() throws IOException {
        final LoggerStore store = new Log4JLoggerStore(getInputStream(LOG4J_XML));
        runLoggerTest(LOG4J_XML_FILENAME, store, ConsoleLogger.LEVEL_DEBUG);
    }

    @Test
    public void testLog4JInputStreamConfigurationNoDebug() throws IOException {
        final LoggerStore store = new Log4JLoggerStore(getInputStream(LOG4J_XML));
        runLoggerTest(LOG4J_XML_FILENAME, store, ConsoleLogger.LEVEL_NONE);
    }

    @Test
    public void testLog4JInputStreamConfigurationNoLog() throws IOException {
        final LoggerStore store = new Log4JLoggerStore(getInputStream(LOG4J_XML));
        runLoggerTest(LOG4J_XML_FILENAME, store);
    }

    @Test
    public void testLog4JPropertiesConfiguration() throws IOException {
        final Properties properties = new Properties();
        properties.load(getInputStream(LOG4J_PROPERTIES));
        final LoggerStore store = new Log4JLoggerStore(properties);
        runLoggerTest(LOG4J_PROPERTIES_FILENAME, store, ConsoleLogger.LEVEL_DEBUG);
    }

    @Test
    public void testLog4JPropertiesConfigurationNoDebug() throws IOException {
        final Properties properties = new Properties();
        properties.load(getInputStream(LOG4J_PROPERTIES));
        final LoggerStore store = new Log4JLoggerStore(properties);
        runLoggerTest(LOG4J_PROPERTIES_FILENAME, store, ConsoleLogger.LEVEL_NONE);
    }

    @Test
    public void testLog4JPropertiesConfigurationNoLog() throws IOException {
        final Properties properties = new Properties();
        properties.load(getInputStream(LOG4J_PROPERTIES));
        final LoggerStore store = new Log4JLoggerStore(properties);
        runLoggerTest(LOG4J_PROPERTIES_FILENAME, store);
    }

    // JDKLoggerStore tests
    @Test
    public void testJDKConfiguration() throws IOException {
        final LoggerStore store = new JdkLoggerStore(getInputStream(LOGGING_PROPERTIES));
        runLoggerTest(JDK_FILENAME, store, ConsoleLogger.LEVEL_DEBUG);
    }

    @Test
    public void testJDKConfigurationNoDebug() throws IOException {
        final LoggerStore store = new JdkLoggerStore(getInputStream(LOGGING_PROPERTIES));
        runLoggerTest(JDK_FILENAME, store, ConsoleLogger.LEVEL_NONE);
    }

    @Test
    public void testJDKConfigurationNoLog() throws IOException {
        final LoggerStore store = new JdkLoggerStore(getInputStream(LOGGING_PROPERTIES));
        runLoggerTest(JDK_FILENAME, store);
    }

}
