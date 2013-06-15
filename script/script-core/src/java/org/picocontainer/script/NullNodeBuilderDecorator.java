/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script;

import java.util.Map;

import org.picocontainer.ComponentFactory;
import org.picocontainer.MutablePicoContainer;

/**
 * Null-object implementation of NodeBuilderDecorator
 *
 * @author Aslak Helles&oslash;y
 * @author Paul Hammant
 */
@SuppressWarnings({"rawtypes"})
public class NullNodeBuilderDecorator implements NodeBuilderDecorator {
	public ComponentFactory decorate(final ComponentFactory componentFactory, final Map attributes) {
        return componentFactory;
    }

    public MutablePicoContainer decorate(final MutablePicoContainer picoContainer) {
        return picoContainer;
    }

    public Object createNode(final Object name, final Map attributes, final Object parentElement) {
        throw new ScriptedPicoContainerMarkupException("Don't know how to create a '" + name + "' child of a '"
                + ((parentElement == null) ? "null" : parentElement.toString()) + "' element");
    }

    public void rememberComponentKey(final Map attributes) {
        //Does nothing.
    }
}
