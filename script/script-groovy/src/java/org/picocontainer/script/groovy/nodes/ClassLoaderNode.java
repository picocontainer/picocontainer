/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.groovy.nodes;

import java.util.Map;

import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;

/**
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public class ClassLoaderNode extends AbstractBuilderNode {

    public static final String NODE_NAME = "classLoader";

    public ClassLoaderNode() {
        super(NODE_NAME);
    }

    public Object createNewNode(final Object current, final Map<String, Object> attributes) {

        ClassLoadingPicoContainer container = (ClassLoadingPicoContainer) current;
        return new DefaultClassLoadingPicoContainer(container.getComponentClassLoader(), container);
    }

}
