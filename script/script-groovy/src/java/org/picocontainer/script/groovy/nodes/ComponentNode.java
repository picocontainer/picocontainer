/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.groovy.nodes;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.picocontainer.Parameter;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.script.NodeBuilderDecorator;
import org.picocontainer.script.util.ComponentElementHelper;

/**
 * Creates a component node
 *
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class ComponentNode extends AbstractBuilderNode {

    public static final String NODE_NAME = "component";

    /**
     * Attributes 'key'
     */
    public static final String KEY = "key";

    /**
     * Class attribute.
     */
    private static final String CLASS = "class";

    /**
     * Class Name Key Attribute.
     */
    private static final String CLASS_NAME_KEY = "classNameKey";

    /**
     * Instance attribute name.
     */
    private static final String INSTANCE = "instance";

    /**
     * Parameters attribute name.
     */
    private static final String PARAMETERS = "parameters";

    /**
     * Properties attribute name.
     */
    private static final String PROPERTIES = "properties";

    private final NodeBuilderDecorator decorator;

    public ComponentNode(final NodeBuilderDecorator decorator) {
        super(NODE_NAME);
        this.decorator = decorator;
        // Supported attributes.
        this.addAttribute(KEY).addAttribute(CLASS).addAttribute(CLASS_NAME_KEY).addAttribute(INSTANCE).addAttribute(
                PARAMETERS).addAttribute(PROPERTIES);
    }

    /**
     * Execute the handler for the given node builder.
     *
     * @param current The current node.
     * @param attributes Map attributes specified in the groovy script for the
     *            builder node.
     * @return Object
     */
    public Object createNewNode(final Object current, final Map<String, Object> attributes) {
        decorator.rememberComponentKey(attributes);
        Object key = attributes.remove(KEY);
        Object classNameKey = attributes.remove(CLASS_NAME_KEY);
        Object classValue = attributes.remove(CLASS);
        Object instance = attributes.remove(INSTANCE);
        Object parameters = attributes.remove(PARAMETERS);
        Object properties = attributes.remove(PROPERTIES);

        return ComponentElementHelper.makeComponent(classNameKey, key, getParameters(parameters), classValue,
                (ClassLoadingPicoContainer) current, instance, getProperties(properties));
    }

    @SuppressWarnings("unchecked")
    private static Parameter[] getParameters(final Object params) {
        if (params == null) {
            return null;
        }

        if (params instanceof Parameter[]) {
            return (Parameter[]) params;
        }

        if (!(params instanceof List)) {
            throw new IllegalArgumentException("Parameters may only be of type List or Parameter Array");
        }

        List<Parameter> list = (List<Parameter>) params;

        int n = list.size();
        Parameter[] parameters = new Parameter[n];
        for (int i = 0; i < n; ++i) {
            parameters[i] = toParameter(list.get(i));
        }
        return parameters;
    }

    private static Parameter toParameter(final Object obj) {
        return obj instanceof Parameter ? (Parameter) obj : new ConstantParameter(obj);
    }

    @SuppressWarnings("unchecked")
    private static Properties[] getProperties(final Object props) {
        if (props == null) {
            return new Properties[0];
        }
        if (!(props instanceof List)) {
            throw new IllegalArgumentException("Properties may only be of type List");
        }

        List<Properties> list = (List<Properties>) props;
        return list.toArray(new Properties[list.size()]);
    }

}
