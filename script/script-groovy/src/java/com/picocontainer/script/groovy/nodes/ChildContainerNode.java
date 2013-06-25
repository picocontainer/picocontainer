/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script.groovy.nodes;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import com.picocontainer.script.NodeBuilderDecorator;
import com.picocontainer.script.ScriptedPicoContainerMarkupException;

import com.picocontainer.ComponentFactory;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.ComponentMonitorStrategy;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.classname.ClassLoadingPicoContainer;
import com.picocontainer.classname.DefaultClassLoadingPicoContainer;
import com.picocontainer.monitors.AbstractComponentMonitor;

/**
 * Creates a new PicoContainer node. There may or may not be a parent container
 * involved.
 *
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class ChildContainerNode extends AbstractBuilderNode {

    /**
     * Node name.
     */
    public static final String NODE_NAME = "container";

    /**
     * Supported Attribute: 'class' Reference to a classname of the container to
     * use.
     */
    private static final String CLASS = "class";

    /**
     * The node decorator.
     */
    private final NodeBuilderDecorator decorator;

    /**
     * Attribute: 'componentFactory' a reference to an instance of a component
     * factory.
     */
    private static final String COMPONENT_ADAPTER_FACTORY = "componentFactory";

    /**
     * Attribute: 'monitor' a reference to an instance of a component
     * monitor.
     */
    private static final String COMPONENT_MONITOR = "monitor";

    /**
     * Attribute that exists in test cases, but not necessarily used?
     */
    private static final String SCOPE = "scope";

    /**
     * Attribute: 'parent' a reference to the parent for this new container.
     */
    private static final String PARENT = "parent";

    /**
     * Constructs a child container node with a given decorator
     *
     * @param decorator NodeBuilderDecorator
     */
    public ChildContainerNode(final NodeBuilderDecorator decorator) {
        super(NODE_NAME);
        this.decorator = decorator;

        this.addAttribute(CLASS).addAttribute(COMPONENT_ADAPTER_FACTORY).addAttribute(COMPONENT_MONITOR).addAttribute(
                PARENT).addAttribute(SCOPE);

    }

    /**
     * Creates a new container. There may or may not be a parent to this
     * container. Supported attributes are
     * <p>
     * {@inheritDoc}
     * </p>
     *
     * @param current PicoContainer
     * @param attributes Map
     * @return Object
     * @throws ScriptedPicoContainerMarkupException
     */
    @SuppressWarnings("unchecked")
    public Object createNewNode(final Object current, final Map attributes) throws ScriptedPicoContainerMarkupException {

        return createChildContainer(attributes, (ClassLoadingPicoContainer) current);
    }

    /**
     * Returns the decorator
     *
     * @return A NodeBuilderDecorator
     */
    private NodeBuilderDecorator getDecorator() {
        return decorator;
    }

    /**
     * Creates a new container. There may or may not be a parent to this
     * container. Supported attributes are:
     * <ul>
     * <li><tt>componentFactory</tt>: The ComponentFactory used for new
     * container</li>
     * <li><tt>monitor</tt>: The ComponentMonitor used for new
     * container</li>
     * </ul>
     *
     * @param attributes Map Attributes defined by the builder in the script.
     * @param parent The parent container
     * @return The PicoContainer
     */
    @SuppressWarnings("unchecked")
    protected ClassLoadingPicoContainer createChildContainer(final Map<String,Object> attributes, final ClassLoadingPicoContainer parent) {

        ClassLoader parentClassLoader;
        MutablePicoContainer childContainer;
        if (parent != null) {
            parentClassLoader = parent.getComponentClassLoader();
            if (isAttribute(attributes, COMPONENT_ADAPTER_FACTORY)) {
                ComponentFactory componentFactory = createComponentFactory(attributes);
                childContainer = new DefaultPicoContainer(parent, getDecorator()
                        .decorate(componentFactory, attributes));
                if (isAttribute(attributes, COMPONENT_MONITOR)) {
                    changeComponentMonitor(childContainer, createComponentMonitor(attributes));
                }
                parent.addChildContainer(childContainer);
            } else if (isAttribute(attributes, COMPONENT_MONITOR)) {
                ComponentFactory componentFactory = new Caching();
                childContainer = new DefaultPicoContainer(parent, getDecorator()
                        .decorate(componentFactory, attributes));
                changeComponentMonitor(childContainer, createComponentMonitor(attributes));
            } else {
                childContainer = parent.makeChildContainer();
            }
        } else {
            parentClassLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return PicoContainer.class.getClassLoader();
                }
            });
            ComponentFactory componentFactory = createComponentFactory(attributes);
            childContainer = new DefaultPicoContainer(getDecorator().decorate(componentFactory, attributes));
            if (isAttribute(attributes, COMPONENT_MONITOR)) {
                changeComponentMonitor(childContainer, createComponentMonitor(attributes));
            }
        }

        MutablePicoContainer decoratedPico = getDecorator().decorate(childContainer);
        if (isAttribute(attributes, CLASS)) {
            Class<?> clazz = (Class<?>) attributes.get(CLASS);
            return createPicoContainer(clazz, decoratedPico, parentClassLoader);
        } else {
            return new DefaultClassLoadingPicoContainer(parentClassLoader, decoratedPico);
        }
    }

    private void changeComponentMonitor(final MutablePicoContainer childContainer, final ComponentMonitor monitor) {
        if (childContainer instanceof ComponentMonitorStrategy) {
            ((ComponentMonitorStrategy) childContainer).changeMonitor(monitor);
        }
    }

    private ClassLoadingPicoContainer createPicoContainer(final Class<?> clazz, final MutablePicoContainer decoratedPico,
            final ClassLoader parentClassLoader) {
        DefaultPicoContainer instantiatingContainer = new DefaultPicoContainer();
        instantiatingContainer.addComponent(ClassLoader.class, parentClassLoader);
        instantiatingContainer.addComponent(MutablePicoContainer.class, decoratedPico);
        instantiatingContainer.addComponent(ClassLoadingPicoContainer.class, clazz);
        Object componentInstance = instantiatingContainer.getComponent(ClassLoadingPicoContainer.class);
        return (ClassLoadingPicoContainer) componentInstance;
    }

    private ComponentFactory createComponentFactory(final Map<String,Object> attributes) {
        final ComponentFactory factory = (ComponentFactory) attributes.remove(COMPONENT_ADAPTER_FACTORY);
        if (factory == null) {
            return new Caching();
        }
        return factory;
    }

    private ComponentMonitor createComponentMonitor(final Map<String,Object> attributes) {
        final ComponentMonitor monitor = (ComponentMonitor) attributes.remove(COMPONENT_MONITOR);
        if (monitor == null) {
            return new AbstractComponentMonitor();
        }
        return monitor;
    }

}
