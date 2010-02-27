/*
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.picocontainer.logging.store;

import static org.picocontainer.logging.loggers.ConsoleLogger.LEVEL_DEBUG;
import static org.picocontainer.logging.loggers.ConsoleLogger.LEVEL_NONE;
import static org.picocontainer.logging.store.Configurator.JDK;
import static org.picocontainer.logging.store.Configurator.LOG4J_DOM;
import static org.picocontainer.logging.store.Configurator.LOG4J_PROPERTY;
import static org.picocontainer.logging.store.Configurator.createLoggerStore;

import java.io.IOException;

import org.junit.Test;

/**
 * @author Mauro Talevi
 * @author Peter Donald
 */
public class ConfiguratorTest extends AbstractTest {

    private static final String LOGGING_PROPERTIES = "org/picocontainer/logging/store/logging.properties";
    private static final String JDK_FILENAME = "jdk";
    private static final String LOG4J_PROPERTIES_FILENAME = "log4j-properties";
    private static final String LOG4J_PROPERTIES = "org/picocontainer/logging/store/log4j.properties";
    private static final String LOG4J_XML_FILENAME = "log4j-xml";
    private static final String LOG4J_XML = "org/picocontainer/logging/store/log4j.xml";

    @Test(expected = LoggerStoreCreationException.class)
    public void testInvalidConfiguratorType() throws IOException {
        createLoggerStore("blah", LOGGING_PROPERTIES);
    }

    @Test
    public void testLog4JDOMConfigurator() throws IOException {
        runLoggerTest(LOG4J_XML_FILENAME, createLoggerStore(LOG4J_DOM, getInputStream(LOG4J_XML)), LEVEL_DEBUG);
    }

    @Test
    public void testLog4JDOMConfiguratorNoDebug() throws IOException {
        runLoggerTest(LOG4J_XML_FILENAME, createLoggerStore(LOG4J_DOM, getInputStream(LOG4J_XML)), LEVEL_NONE);
    }

    @Test
    public void testLog4JDOMConfiguratorNoLog() throws IOException {
        runLoggerTest(LOG4J_XML_FILENAME, createLoggerStore(LOG4J_DOM, getInputStream(LOG4J_XML)));
    }

    @Test
    public void testLog4JPropertyConfigurator() throws IOException {
        runLoggerTest(LOG4J_PROPERTIES_FILENAME, createLoggerStore(LOG4J_PROPERTY, getInputStream(LOG4J_PROPERTIES)),
                LEVEL_DEBUG);
    }

    @Test
    public void testLog4JPropertyConfiguratorNoDebug() throws IOException {
        runLoggerTest(LOG4J_PROPERTIES_FILENAME, createLoggerStore(LOG4J_PROPERTY, getInputStream(LOG4J_PROPERTIES)),
                LEVEL_NONE);
    }

    @Test
    public void testLog4JPropertyConfiguratorNoLog() throws IOException {
        runLoggerTest(LOG4J_PROPERTIES_FILENAME, createLoggerStore(LOG4J_PROPERTY, getInputStream(LOG4J_PROPERTIES)));
    }

    @Test
    public void testJDKConfigurator() throws IOException {
        runLoggerTest(JDK_FILENAME, createLoggerStore(JDK, getInputStream(LOGGING_PROPERTIES)), LEVEL_DEBUG);
    }

    @Test
    public void testJDKConfiguratorNoDebug() throws IOException {
        runLoggerTest(JDK_FILENAME, createLoggerStore(JDK, getInputStream(LOGGING_PROPERTIES)), LEVEL_NONE);
    }

    @Test
    public void testJDKConfiguratorNoLog() throws IOException {
        runLoggerTest(JDK_FILENAME, createLoggerStore(JDK, getInputStream(LOGGING_PROPERTIES)));
    }

}
