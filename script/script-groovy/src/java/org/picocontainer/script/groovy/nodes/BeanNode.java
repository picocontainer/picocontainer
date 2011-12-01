/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file. 
 ******************************************************************************/
package org.picocontainer.script.groovy.nodes;

import java.util.Map;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.script.ScriptedPicoContainerMarkupException;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Creates on-the-spot Javabeans configurations and registers the result with
 * the container @link MutablePicoContainer#addComponent}.
 * 
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class BeanNode extends AbstractBuilderNode {

    /**
     * The name of the node we're handling.
     */
    public static final String NODE_NAME = "bean";

    /**
     * Bean class attribute.
     */
    public static final String BEAN_CLASS = "beanClass";


    /**
     * Default constructor.
     */
    public BeanNode() {
        super(NODE_NAME);
    }

    public Object createNewNode(Object current,  Map<String, Object> attributes) {
        Object bean = createBean(attributes);
        ((MutablePicoContainer) current).addComponent(bean);
        return bean;
    }


    /**
     * Instantiates the bean and sets the appropriate attributes.  It then
     * @param attributes Map
     * @return Object resulting JavaBean.
     */
    @SuppressWarnings("unchecked")
    protected Object createBean(final  Map<String, Object> attributes) {
        Class<?> type = (Class<?>) attributes.remove(BEAN_CLASS);
        if (type == null) {
            throw new ScriptedPicoContainerMarkupException("Bean must have a beanClass attribute");
        }
        try {
            Object bean = type.newInstance();
            // now let's set the properties on the bean
            for (Object o : attributes.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                InvokerHelper.setProperty(bean, name, value);
            }
            return bean;
        } catch (IllegalAccessException e) {
            throw new ScriptedPicoContainerMarkupException("Failed to create bean of type '" + type + "'. Reason: " + e, e);
        } catch (InstantiationException e) {
            throw new ScriptedPicoContainerMarkupException("Failed to create bean of type " + type + "'. Reason: " + e, e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>This version only checks for 'beanClass' and lets all other attributes
     * through (since they become property values)</p>
     * @param specifiedAttributes Map
     * @throws ScriptedPicoContainerMarkupException
     */
    public void validateScriptedAttributes(Map<String, Object> specifiedAttributes) throws ScriptedPicoContainerMarkupException {
        if (!specifiedAttributes.containsKey(BEAN_CLASS)) {
            throw new ScriptedPicoContainerMarkupException("Attribute " + BEAN_CLASS + " is required.");
        }
        //Assume all other attributes
    }
}
