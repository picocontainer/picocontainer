/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.xml;

import org.picocontainer.PicoContainer;
import org.w3c.dom.Element;

/**
 * Factory that creates instances from DOM Elements
 *
 * @author Paul Hammant
 * @author Marcos Tarruella
 */
public interface XMLComponentInstanceFactory {
    /**
     * Creates an instance of an Object from a DOM Element
     *
     * @param container the PicoContainer
     * @param element the DOM Element
     * @param classLoader the ClassLoader
     * @return An Object instance
     */
    Object makeInstance(PicoContainer container, Element element, ClassLoader classLoader);
}
