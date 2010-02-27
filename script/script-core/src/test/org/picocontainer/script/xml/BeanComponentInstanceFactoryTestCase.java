/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.script.xml;

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

/**
 * @author Paul Hammant
 * @author Marcos Tarruella
 */
public class BeanComponentInstanceFactoryTestCase {

    @Test public void testDeserialization() throws ParserConfigurationException, IOException, SAXException {
        BeanComponentInstanceFactory factory = new BeanComponentInstanceFactory();

        StringReader sr = new StringReader("" +
                "<org.picocontainer.script.xml.TestBean>" +
                "<foo>10</foo>" +
                "<bar>hello</bar>" +
                "</org.picocontainer.script.xml.TestBean>");
        InputSource is = new InputSource(sr);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(is);

        Object o = factory.makeInstance(null, doc.getDocumentElement(), Thread.currentThread().getContextClassLoader());
        TestBean bean = (TestBean) o;
        assertEquals("hello", bean.getBar());
        assertEquals(10, bean.getFoo());
    }

    @Test public void testDeserializationWithMappedName() throws ParserConfigurationException, IOException, SAXException {
        BeanComponentInstanceFactory factory = new BeanComponentInstanceFactory();

        StringReader sr = new StringReader("" +
                "<org.picocontainer.script.xml.TestBean>" +
                "<any name='foo'>10</any>" +
                "<bar>hello</bar>" +
                "</org.picocontainer.script.xml.TestBean>");
        InputSource is = new InputSource(sr);
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(is);

        Object o = factory.makeInstance(null, doc.getDocumentElement(), Thread.currentThread().getContextClassLoader());
        TestBean bean = (TestBean) o;
        assertEquals("hello", bean.getBar());
        assertEquals(10, bean.getFoo());
    }
}
