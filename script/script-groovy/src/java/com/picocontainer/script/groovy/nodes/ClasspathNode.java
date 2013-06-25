/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script.groovy.nodes;

import java.util.Map;

import com.picocontainer.script.util.ClassPathElementHelper;

import com.picocontainer.classname.ClassLoadingPicoContainer;
import com.picocontainer.classname.ClassPathElement;


/**
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class ClasspathNode extends AbstractBuilderNode {

    public static final String NODE_NAME = "classPathElement";


    private static final String PATH = "path";


    public ClasspathNode() {
        super(NODE_NAME);

        addAttribute(PATH);
    }


    @SuppressWarnings("unchecked")
    public Object createNewNode(final Object current, final Map attributes) {
        return createClassPathElementNode(attributes, (ClassLoadingPicoContainer) current);
    }

    @SuppressWarnings("unchecked")
    private ClassPathElement createClassPathElementNode(final Map attributes, final ClassLoadingPicoContainer container) {

        final String path = (String) attributes.remove(PATH);
        return ClassPathElementHelper.addClassPathElement(path, container);
    }

}
