/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.script.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;

/**
 * @author Paul Hammant
 * @author Marcos Tarruella
 */
public class XStreamComponentInstanceFactoryTestCase {

    @Test public void testDeserializationWithDefaultMode() throws ParserConfigurationException, IOException, SAXException {
        runDeserializationTest(new XStreamComponentInstanceFactory());
    }

    @Test public void testDeserializationInEncancedMode() throws ParserConfigurationException, IOException, SAXException {
        runDeserializationTest(new XStreamComponentInstanceFactory(new XStream(new Sun14ReflectionProvider())));
    }

    @Test public void testDeserializationInPureJavaMode() throws ParserConfigurationException, IOException, SAXException {
        runDeserializationTest(new PureJavaXStreamComponentInstanceFactory());
    }

    public void runDeserializationTest(final XMLComponentInstanceFactory factory) throws ParserConfigurationException, IOException, SAXException {
        StringReader sr = new StringReader("" +
                "<com.picocontainer.script.xml.TestBean>" +
                "<foo>10</foo>" +
                "<bar>hello</bar>" +
                "</com.picocontainer.script.xml.TestBean>");
        InputSource is = new InputSource(sr);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(is);

        Object o = factory.makeInstance(null, doc.getDocumentElement(), Thread.currentThread().getContextClassLoader());
        TestBean bean = (TestBean) o;
        assertEquals("hello", bean.getBar());
        assertEquals(10, bean.getFoo());
    }

}
