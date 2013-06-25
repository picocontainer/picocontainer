/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script.groovy.nodes;

import java.util.Map;

/**
 * Handles 'doCall' nodes.
 *
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class DoCallNode extends AbstractBuilderNode {

    public static final String NODE_NAME = "doCall";

    public DoCallNode() {
        super(NODE_NAME);
    }

    public Object createNewNode(final Object current, final Map<String,Object> attributes) {
        // TODO does this node need to be handled?
        return null;
    }
}
