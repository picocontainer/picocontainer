/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.groovy.nodes;

import java.util.Map;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.script.ScriptedPicoContainerMarkupException;

/**
 * Config node adds configuration entry to mutable pico container. It requires two
 * named parameters: key and value.  Example usage
 * <pre>
 * config(key:'foo',value:'bar')
 * </pre>
 * 
 * @author k.pribluda
 */
@SuppressWarnings("serial")
public class ConfigNode extends AbstractBuilderNode {

    public static final String NODE_NAME = "config";

    /**
     * attribute name for key attribute ( Required )
     */
    public static final String KEY = "key";
    /**
     * attribute name for value attribute ( Required )
     */
    public static final String VALUE = "value";

    public ConfigNode() {
        super(NODE_NAME);
    }

    public Object createNewNode(Object current, Map<String, Object> attributes) {
        validateScriptedAttributes(attributes);
        ((MutablePicoContainer) current).addConfig((String) attributes.get(KEY), attributes.get(VALUE));
        return null;
    }

    /**
     * ansure that node has proper attributes
     */
    public void validateScriptedAttributes(Map<String, Object> specifiedAttributes)
            throws ScriptedPicoContainerMarkupException {
        if (specifiedAttributes.size() != 2 || !isAttribute(specifiedAttributes, KEY)
                || !isAttribute(specifiedAttributes, VALUE)) {
            throw new ScriptedPicoContainerMarkupException("config has two parameters - key and value");
        }
    }
}
