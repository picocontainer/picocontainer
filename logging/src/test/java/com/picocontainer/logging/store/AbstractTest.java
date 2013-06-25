/*
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.picocontainer.logging.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static com.picocontainer.logging.store.LoggerStoreFactory.FILE_LOCATION;
import static com.picocontainer.logging.store.LoggerStoreFactory.URL_LOCATION;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import com.picocontainer.logging.Logger;
import com.picocontainer.logging.store.stores.JdkLoggerStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Mauro Talevi
 * @author Peter Donald
 */
public abstract class AbstractTest {

    protected static final String MESSAGE = "Testing Logger";
    protected static final String MESSAGE2 = "This occurs in sub-category";

    private File logsDir;

    @Before
    public void setUp() throws Exception {
        this.logsDir = new File("target/logs");
        this.logsDir.mkdirs();
    }

    protected final InputStream getInputStream(final String name) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is =  classLoader.getResourceAsStream(name);
        if (is == null) {
        	throw new IllegalArgumentException("Couldn't load " + name + " in classloader");
        }
        
        return is;
    }

    protected final URL getURL(final String name) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResource(name);
    }

    protected void runConsoleLoggerTest(final LoggerStore store, final int level) {
        final Logger logger = store.getLogger();
        assertNotNull("rootLogger for console", logger);
        logger.info(MESSAGE);
        final Logger noExistLogger = store.getLogger("no-exist");
        assertNotNull("noExistLogger for console", noExistLogger);
        noExistLogger.info(MESSAGE2);
        store.close();
    }

    protected void runLoggerTest(final String filename, final LoggerStore store) throws IOException {
        BufferedReader reader = null;
        try {
            final Logger logger = store.getLogger();
            assertNotNull("rootLogger for " + filename, logger);
            logger.info(MESSAGE);
            final Logger noExistLogger = store.getLogger("no-exist");
            assertNotNull("noExistLogger for " + filename, noExistLogger);
            noExistLogger.info(MESSAGE2);

            assertEquals("Same Logger returned multiple times:", noExistLogger, store.getLogger("no-exist"));

            try {
                store.getLogger(null);
                fail("Expected a NullPointerException when passing " + "null in for getLogger parameter");
            } catch (final NullPointerException npe) {
                assertEquals("NullPointer message", "name", npe.getMessage());
            }

            final File logFile = new File(this.logsDir, filename + ".log");
            assertTrue("Log file should exist: " + logFile, logFile.exists());

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
            assertEquals("First line Contents for logger" + filename, MESSAGE, reader.readLine());
            assertEquals("Second line Contents for logger" + filename, MESSAGE2, reader.readLine());
            assertNull("Third Line Contents for logger" + filename, reader.readLine());
            reader.close();
            logFile.delete();

            if (!(store instanceof JdkLoggerStore)) {
                final Logger nejney = store.getLogger("nejney");
                nejney.info(MESSAGE);

                final File logFile2 = new File(this.logsDir, filename + "2.log");
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile2)));
                assertEquals("First line Contents for nejney logger" + filename, MESSAGE, reader.readLine());
                assertNull("Second Line Contents for nejney logger" + filename, reader.readLine());
                reader.close();
                logFile2.delete();
            }
        } finally {
            store.close();
            if (null != reader) {
                reader.close();
            }
        }
    }

    protected void runLoggerTest(final String filename, final LoggerStore store, final int level) throws IOException {
        runLoggerTest(filename, store);
    }

    protected void runStreamBasedFactoryTest(final String inputFile, final LoggerStoreFactory factory,
            final String outputFile, final HashMap<String, Object> inputData, final int level) throws IOException {
        // URL should be in file: format
        final URL url = getURL(inputFile);
        assertEquals("URL is of file type", url.getProtocol(), "file");

        final HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(URL_LOCATION, url.toExternalForm());
        config.putAll(inputData);
        runFactoryTest(factory, config, outputFile, level);
        final HashMap<String, Object> config2 = new HashMap<String, Object>();
        config2.put(URL.class.getName(), url);
        config2.putAll(inputData);
        runFactoryTest(factory, config2, outputFile, level);
        final String filename = url.toExternalForm().substring(5);
        final HashMap<String, Object> config3 = new HashMap<String, Object>();
        config3.put(FILE_LOCATION, filename);
        config3.putAll(inputData);
        runFactoryTest(factory, config3, outputFile, level);
        final HashMap<String, Object> config4 = new HashMap<String, Object>();
        config4.put(File.class.getName(), new File(filename));
        config4.putAll(inputData);
        runFactoryTest(factory, config4, outputFile, level);
        final HashMap<String, Object> config5 = new HashMap<String, Object>();
        config5.put(InputStream.class.getName(), new FileInputStream(filename));
        config5.putAll(inputData);
        runFactoryTest(factory, config5, outputFile, level);
    }

    protected void runFactoryTest(final LoggerStoreFactory factory, final HashMap<String, Object> config,
            final String filename, final int level) throws IOException {
        final LoggerStore store = factory.createLoggerStore(config);
        runLoggerTest(filename, store, level);
    }

    protected static Element buildElement(final InputStream resource, final EntityResolver resolver,
            final String systemId) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        if (resolver != null) {
            db.setEntityResolver(resolver);
        }
        InputSource source = new InputSource(resource);
        if (systemId != null) {
            source.setSystemId(systemId);
        }
        Document doc = db.parse(source);
        return doc.getDocumentElement();
    }

    protected void createLoggerStoreWithEmptyConfiguration(final LoggerStoreFactory factory) {
        try {
            factory.createLoggerStore(new HashMap<String, Object>());
            fail("Expected LoggerStoreCreationException");
        } catch (LoggerStoreCreationException e) {
            // expected
        }
    }

}
