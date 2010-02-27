/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.ComponentFactory;

import java.util.Map;

/**
 * Null-object implementation of NodeBuilderDecorator
 * 
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 */
@SuppressWarnings({"unchecked","unused"})
public class NullNodeBuilderDecorator implements NodeBuilderDecorator {
    public ComponentFactory decorate(ComponentFactory componentFactory, Map attributes) {
        return componentFactory;
    }

    public MutablePicoContainer decorate(MutablePicoContainer picoContainer) {
        return picoContainer;
    }

    public Object createNode(Object name, Map attributes, Object parentElement) {
        throw new ScriptedPicoContainerMarkupException("Don't know how to create a '" + name + "' child of a '"
                + ((parentElement == null) ? "null" : parentElement.toString()) + "' element");
    }

    public void rememberComponentKey(Map attributes) {
        //Does nothing.
    }
}
