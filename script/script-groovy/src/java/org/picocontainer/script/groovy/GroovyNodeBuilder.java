/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.groovy;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.util.BuilderSupport;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.script.NodeBuilderDecorator;
import org.picocontainer.script.NullNodeBuilderDecorator;
import org.picocontainer.script.ScriptedPicoContainerMarkupException;
import org.picocontainer.script.groovy.nodes.AppendContainerNode;
import org.picocontainer.script.groovy.nodes.BeanNode;
import org.picocontainer.script.groovy.nodes.ChildContainerNode;
import org.picocontainer.script.groovy.nodes.ClassLoaderNode;
import org.picocontainer.script.groovy.nodes.ClasspathNode;
import org.picocontainer.script.groovy.nodes.ComponentNode;
import org.picocontainer.script.groovy.nodes.ConfigNode;
import org.picocontainer.script.groovy.nodes.DoCallNode;
import org.picocontainer.script.groovy.nodes.GrantNode;
import org.picocontainer.script.groovy.nodes.NewBuilderNode;

/**
 * Builds node trees of PicoContainers and Pico components using GroovyMarkup.
 * <p>
 * Simple example usage in your groovy script:
 *
 * <pre>
 * builder = new org.picocontainer.script.groovy.GroovyNodeBuilder()
 * pico = builder.container(parent:parent) {
 *   component(class:org.picocontainer.script.testmodel.DefaultWebServerConfig)
 *   component(class:org.picocontainer.script.testmodel.WebServerImpl)
 * }
 * </pre>
 *
 * </p>
 * <h4>Extending/Enhancing GroovyNodeBuilder</h4>
 * <p>
 * Often-times people need there own assembly commands that are needed for
 * extending/enhancing the node builder tree. For example, a typical extension
 * may be to provide AOP vocabulary for the node builder with terms such as
 * 'aspect', 'pointcut', etc.
 * </p>
 * <p>
 * GroovyNodeBuilder provides two primary ways of enhancing the nodes supported
 * by the groovy builder:
 * {@link org.picocontainer.script.NodeBuilderDecorator NodeBuilderDecorator}
 * and special node handlers {@link BuilderNode BuilderNode}. Using
 * NodeBuilderDecorator is often a preferred method because it is ultimately
 * script independent. However, replacing an existing GroovyNodeBuilder's
 * behavior is currently the only way to replace the behavior of an existing
 * groovy node handler.
 * </p>
 *
 * @author James Strachan
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Michael Rimov
 * @author Mauro Talevi
 */
@SuppressWarnings("unchecked")
public class GroovyNodeBuilder extends BuilderSupport {

    private static final String CLASS = "class";

    private static final String PARENT = "parent";

    /**
     * Flag indicating that the attribute validation should be performed.
     */
    public static final boolean PERFORM_ATTRIBUTE_VALIDATION = true;

    /**
     * Flag indicating that attribute validation should be skipped.
     */
    public static final boolean SKIP_ATTRIBUTE_VALIDATION = false;

    /**
     * Decoration delegate. The traditional method of adding functionality to
     * the Groovy builder.
     */
    private final NodeBuilderDecorator decorator;

    /**
     * Map of node handlers.
     */
    private final Map nodeBuilderHandlers = new HashMap();
    private final Map nodeBuilders = new HashMap();

    private final boolean performAttributeValidation;

    /**
     * Allows the composition of a <tt>{@link NodeBuilderDecorator}</tt> -- an
     * object that extends the capabilities of the <tt>GroovyNodeBuilder</tt>
     * with new tags, new capabilities, etc.
     *
     * @param decorator NodeBuilderDecorator
     * @param performAttributeValidation should be set to
     *            PERFORM_ATTRIBUTE_VALIDATION or SKIP_ATTRIBUTE_VALIDATION
     */
    public GroovyNodeBuilder(final NodeBuilderDecorator decorator, final boolean performAttributeValidation) {
        this.decorator = decorator;
        this.performAttributeValidation = performAttributeValidation;

        // Build and register node handlers.
        this.setNode(new ComponentNode(decorator)).setNode(new ChildContainerNode(decorator)).setNode(new BeanNode())
                .setNode(new ConfigNode()).setNode(new ClasspathNode()).setNode(new DoCallNode()).setNode(
                        new NewBuilderNode()).setNode(new ClassLoaderNode()).setNode(new GrantNode()).setNode(
                        new AppendContainerNode());

    }

    public GroovyNodeBuilder(final NodeBuilderDecorator decorator) {
        this(decorator, SKIP_ATTRIBUTE_VALIDATION);
    }

    /**
     * Default constructor.
     */
    public GroovyNodeBuilder() {
        this(new NullNodeBuilderDecorator(), SKIP_ATTRIBUTE_VALIDATION);
    }

    @Override
	protected void setParent(final Object parent, final Object child) {
    }

    @Override
	protected Object doInvokeMethod(final String s, final Object name, final Object args) {
        // TODO use setDelegate() from Groovy JSR
        Object answer = super.doInvokeMethod(s, name, args);
        List list = InvokerHelper.asList(args);
        if (!list.isEmpty()) {
            Object o = list.get(list.size() - 1);
            if (o instanceof Closure) {
                Closure closure = (Closure) o;
                closure.setDelegate(answer);
            }
        }
        return answer;
    }

    @Override
	protected Object createNode(final Object name) {
        return createNode(name, Collections.EMPTY_MAP);
    }

    @Override
	protected Object createNode(final Object name, final Object value) {
        Map attributes = new HashMap();
        attributes.put(CLASS, value);
        return createNode(name, attributes);
    }

    /**
     * Override of create node. Called by BuilderSupport. It examines the
     * current state of the builder and the given parameters and dispatches the
     * code to one of the create private functions in this object.
     *
     * @param name The name of the groovy node we're building. Examples are
     *            'container', and 'grant',
     * @param attributes Map attributes of the current invocation.
     * @param value A closure passed into the node. Currently unused.
     * @return Object the created object.
     */
    @Override
	protected Object createNode(final Object name, final Map attributes, final Object value) {
        Object current = getCurrent();
        if (current != null && current instanceof GroovyObject) {
            GroovyObject groovyObject = (GroovyObject) current;
            return groovyObject.invokeMethod(name.toString(), attributes);
        } else if (current == null) {
            current = extractOrCreateValidRootPicoContainer(attributes);
        } else {
            if (attributes.containsKey(PARENT)) {
                throw new ScriptedPicoContainerMarkupException(
                        "You can't explicitly specify a parent in a child element.");
            }
        }
        if (name.equals("registerBuilder")) {
            return registerBuilder(attributes);

        } else {
            return handleNode(name, attributes, current);
        }

    }

    private Object registerBuilder(final Map attributes) {
        String builderName = (String) attributes.remove("name");
        Object clazz = attributes.remove("class");
        try {
            if (clazz instanceof String) {
                clazz = this.getClass().getClassLoader().loadClass((String) clazz);
            }
        } catch (ClassNotFoundException e) {
            throw new ScriptedPicoContainerMarkupException("ClassNotFoundException " + clazz);
        }
        nodeBuilders.put(builderName, clazz);
        return clazz;
    }

    private Object handleNode(final Object name, Map attributes, final Object current) {

        attributes = new HashMap(attributes);

        BuilderNode nodeHandler = this.getNode(name.toString());

        if (nodeHandler == null) {
            Class builderClass = (Class) nodeBuilders.get(name);
            if (builderClass != null) {
                nodeHandler = this.getNode("newBuilder");
                attributes.put("class", builderClass);
            }
        }

        if (nodeHandler == null) {
            // we don't know how to handle it - delegate to the decorator.
            return getDecorator().createNode(name, attributes, current);

        } else {
            // We found a handler.

            if (performAttributeValidation) {
                // Validate
                nodeHandler.validateScriptedAttributes(attributes);
            }

            return nodeHandler.createNewNode(current, attributes);
        }
    }

    /**
     * Pulls the scripted container from the 'current' method or possibly
     * creates a new blank one if needed.
     *
     * @param attributes Map the attributes of the current node.
     * @return ScriptedPicoContainer, never null.
     * @throws ScriptedPicoContainerMarkupException
     */
    private ClassLoadingPicoContainer extractOrCreateValidRootPicoContainer(final Map attributes)
            throws ScriptedPicoContainerMarkupException {
        Object parentAttribute = attributes.get(PARENT);
        //
        // NanoPicoContainer implements MutablePicoCotainer AND PicoContainer
        // So we want to check for PicoContainer first.
        //
        if (parentAttribute instanceof ClassLoadingPicoContainer) {
            // we're not in an enclosing scope - look at parent attribute
            // instead
            return (ClassLoadingPicoContainer) parentAttribute;
        }
        if (parentAttribute instanceof MutablePicoContainer) {
            // we're not in an enclosing scope - look at parent attribute
            // instead
            return new DefaultClassLoadingPicoContainer((MutablePicoContainer) parentAttribute);
        }
        return null;
    }

    /**
     * Returns the current decorator
     *
     * @return A NodeBuilderDecorator, should never be <code>null</code>.
     */
    public NodeBuilderDecorator getDecorator() {
        return this.decorator;
    }

    /**
     * Returns an appropriate node handler for a given node and
     *
     * @param tagName String
     * @return BuilderNode the appropriate node builder for the given tag name,
     *         or null if no handler exists. (In which case, the Delegate
     *         receives the createChildContainer() call)
     */
    public synchronized BuilderNode getNode(final String tagName) {
        Object o = nodeBuilderHandlers.get(tagName);
        return (BuilderNode) o;
    }

    /**
     * Add's a groovy node handler to the table of possible handlers. If a node
     * handler with the same node name already exists in the map of handlers,
     * then the <tt>GroovyNode</tt> replaces the existing node handler.
     *
     * @param newGroovyNode CustomGroovyNode
     * @return GroovyNodeBuilder to allow for method chaining.
     */
    public synchronized GroovyNodeBuilder setNode(final BuilderNode newGroovyNode) {
        nodeBuilderHandlers.put(newGroovyNode.getNodeName(), newGroovyNode);
        return this;
    }

    @Override
	protected Object createNode(final Object name, final Map attributes) {
        return createNode(name, attributes, null);
    }

}
