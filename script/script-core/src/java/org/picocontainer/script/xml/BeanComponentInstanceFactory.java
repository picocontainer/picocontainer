/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.script.xml;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoClassNotFoundException;
import org.picocontainer.behaviors.PropertyApplying;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.injectors.AdaptingInjection;
import org.picocontainer.ComponentFactory;

import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of XMLComponentInstanceFactory that uses PropertyApplicator
 * to create instances from DOM elements.
 *
 * @author Paul Hammant
 * @author Marcos Tarruella
 * @author Mauro Talevi
 */
public class BeanComponentInstanceFactory implements XMLComponentInstanceFactory {
    
    private static final String NAME_ATTRIBUTE = "name";
    
    @SuppressWarnings("unchecked")
    public Object makeInstance(PicoContainer pico, Element element, ClassLoader classLoader) {
        String className = element.getNodeName();
        Object instance;

        if (element.getChildNodes().getLength() == 1) {
            instance = PropertyApplying.PropertyApplicator.convert(className, element.getFirstChild().getNodeValue(), classLoader);
        } else {

            //TODO componentMonitor.newBehavior( .. ) stuff

            PropertyApplying.PropertyApplicator propertyAdapter =
                    new PropertyApplying.PropertyApplicator(createComponentAdapter(className, classLoader));
            java.util.Properties properties = createProperties(element.getChildNodes());
            propertyAdapter.setProperties(properties);
            instance = propertyAdapter.getComponentInstance(pico, ComponentAdapter.NOTHING.class);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private ComponentAdapter createComponentAdapter(String className, ClassLoader classLoader)  {
        Class implementation = loadClass(classLoader, className);
        ComponentFactory factory = new AdaptingInjection();
        return factory.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), new Properties(), className, implementation);
    }

    private Class<?> loadClass(final ClassLoader classLoader, final String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new PicoClassNotFoundException(className, e);
        }
    }

    private java.util.Properties createProperties(NodeList nodes) {
        java.util.Properties properties = new java.util.Properties();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String name = n.getNodeName();
                
                //Provide for a new 'name' attribute in properties.
                if (n.hasAttributes()) {
                    String mappedName = n.getAttributes().getNamedItem(NAME_ATTRIBUTE).getNodeValue();
                    if (mappedName != null) {
                        name = mappedName;
                    }
                }

                String value = n.getFirstChild().getNodeValue();
                properties.setProperty(name, value);
            }
        }
        return properties;
    }
}
