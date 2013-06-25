/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script.xml;

import org.w3c.dom.Element;

import com.picocontainer.PicoContainer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.DomReader;

/**
 * Implementation of XMLComponentInstanceFactory that uses XStream to unmarshal
 * DOM elements.
 *
 * @author Paul Hammant
 * @author Marcos Tarruella
 * @author Mauro Talevi
 */
public class XStreamComponentInstanceFactory implements XMLComponentInstanceFactory {
    /** The XStream used to unmarshal the DOM element */
    private final XStream xstream;

    /**
     * Creates an XStreamComponentInstanceFactory with the default instance of
     * XStream
     */
    public XStreamComponentInstanceFactory() {
        this(new XStream(new DomDriver()));
    }

    /**
     * Creates an XStreamComponentInstanceFactory for a given instance of
     * XStream
     *
     * @param xstream the XStream instance
     */
    public XStreamComponentInstanceFactory(final XStream xstream) {
        this.xstream = xstream;
    }

    public Object makeInstance(final PicoContainer pico, final Element element, final ClassLoader classLoader) {
        return xstream.unmarshal(new DomReader(element));
    }
}
