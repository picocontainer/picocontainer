/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.groovy.nodes;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.picocontainer.script.groovy.BuilderNode;
import java.util.Map;

import org.picocontainer.script.ScriptedPicoContainerMarkupException;

/**
 * Abstract base class for custom nodes. Also provides basic services and
 * construction capabilities.
 * 
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 */
public abstract class AbstractBuilderNode implements BuilderNode, Serializable {

    /**
     * The name of the node we're working with.
     */
    private final String nodeName;

    /**
     * A set of all possible supported attribute names.
     */
    private final Set<String> supportedAttributes = new HashSet<String>();

    /**
     * Constructs a custom node builder. In derived classes you would typically
     * create a default constructor and call addPossibleParent()/addAttribute()
     * to customize the validation capabilities of the Node.
     * 
     * @param nodeName the name of the node we're constructing.
     */
    public AbstractBuilderNode(final String nodeName) {
        this.nodeName = nodeName;

    }

    /**
     * Add an attribute to the list of ones supported by this node.
     * 
     * @param name String the name of the attribute we support.
     * @return AbstractBuilderNode (this) to allow for method chaining.
     */
    protected AbstractBuilderNode addAttribute(final String name) {
        supportedAttributes.add(name);
        return this;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Set<String> getSupportedAttributeNames() {
        return Collections.unmodifiableSet(supportedAttributes);
    }

    public String toString() {
        return "BuilderNode: " + this.getClass().getName() + " (\"" + getNodeName() + "\")";
    }

    /**
     * Checks that an attribute actually exists in the attirbute map. (The key
     * exists and the value is non-null)
     * 
     * @param attributes Map the current node's attributes.
     * @param key String the attribute key we're looking for.
     * @return boolean true if the attribute exists for the current node.
     */
    protected boolean isAttribute(final Map<String, Object> attributes, final String key) {
        return attributes.containsKey(key) && attributes.get(key) != null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This particular implementation checks all specified attribute keynames
     * against the names supported in the node type. It does not type checking
     * against the values passed in via the attributes.
     * </p>
     * 
     * @param specifiedAttributes the attributes as passed in by the groovy
     *            script.
     * @throws ScriptedPicoContainerMarkupException if an attribute is specified
     *             that is not recognized.
     */
    public void validateScriptedAttributes(final Map<String, Object> specifiedAttributes)
            throws ScriptedPicoContainerMarkupException {
        Set<String> specifiedAttributeNames = specifiedAttributes.keySet();
        if (this.getSupportedAttributeNames().containsAll(specifiedAttributeNames)) {
            return;
        }

        Set<String> unknownAttributeNames = new HashSet<String>(specifiedAttributeNames);
        unknownAttributeNames.removeAll(this.getSupportedAttributeNames());

        StringBuffer errorMessage = new StringBuffer();
        errorMessage.append("Found one or more unknown attributes for builder node '");
        errorMessage.append(this.getNodeName());
        errorMessage.append("': ");
        errorMessage.append(toCSV(unknownAttributeNames));
        errorMessage.append(".  Recognized Attributes For this node are [");
        errorMessage.append(toCSV(this.getSupportedAttributeNames()));
        errorMessage.append("].");

        throw new ScriptedPicoContainerMarkupException(errorMessage.toString());
    }

    /**
     * Utility function that takes a set and converts it to a comma delimited
     * String with the format: key1, key2,.....
     * 
     * @param set Set the set to convert. For each object in the set, its
     *            toString() is called.
     * @return String
     */
    private String toCSV(final Set<String> set) {

        StringBuffer result = new StringBuffer();
        boolean needComma = false;
        for (String element : set) {
            if (needComma) {
                result.append(",");
            } else {
                needComma = true;
            }

            result.append(element.toString());
        }
        return result.toString();
    }

}
