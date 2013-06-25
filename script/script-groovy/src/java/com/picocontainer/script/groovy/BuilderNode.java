/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script.groovy;

import java.util.Map;
import java.util.Set;

import com.picocontainer.script.ScriptedPicoContainerMarkupException;

/**
 * In a node builder environment, there is often one class per node that is
 * possible in a builder. This interface provides the necessary validation and
 * interaction methods for the mediator node builder to figure out who should
 * handle what.
 *
 * @author Michael Rimov
 */
public interface BuilderNode {

    /**
     * Returns the name of the node, eg 'container' or 'component'.
     *
     * @return The node name
     */
    String getNodeName();

    /**
     * Returns the supported attribute names.
     *
     * @return The Set of supported attribute names.
     */
    Set<String> getSupportedAttributeNames();

    /**
     * Validates a the attributes as supplied by the node builder against the
     * node's supported attributes.
     *
     * @param attributes the Map of scripted attributes
     * @throws ScriptedPicoContainerMarkupException
     */
    void validateScriptedAttributes(Map<String, Object> attributes) throws ScriptedPicoContainerMarkupException;

    /**
     * Creates a new node .
     *
     * @param current the current Object - may be <code>null</code> for no
     *            parent container.
     * @param attributes the Map of scripted attributes for the builder node -
     *            may be <code>null</code>
     * @return The newly created node
     * @throws ScriptedPicoContainerMarkupException upon script failure to
     *             create new node.
     */
    Object createNewNode(Object current, Map<String, Object> attributes) throws ScriptedPicoContainerMarkupException;

}
