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
 * NodeBuilderDecorators allows to dynamically extend node-based builder syntax,
 * such as <a href="http://picocontainer.org/script/javadoc/groovy/org/picocontainer/script/groovy/GroovyNodeBuilder.html">GroovyNodeBuilder</a>.
 * 
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("unchecked")
public interface NodeBuilderDecorator {

    ComponentFactory decorate(ComponentFactory componentFactory, Map attributes);

    MutablePicoContainer decorate(MutablePicoContainer picoContainer);

    Object createNode(Object name, Map attributes, Object parentElement);

    void rememberComponentKey(Map attributes);

}
