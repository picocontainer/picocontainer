/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.script.xml;

import static com.picocontainer.script.xml.XMLConstants.PARAMETER_ZERO;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.picocontainer.script.ScriptedBuilder;
import com.picocontainer.script.ScriptedContainerBuilder;
import com.picocontainer.script.ScriptedPicoContainerMarkupException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.Parameter;
import com.picocontainer.PicoClassNotFoundException;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.parameters.ComponentParameter;
import com.picocontainer.parameters.ConstantParameter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.DomReader;

/**
 * This class builds up a hierarchy of PicoContainers from an XML configuration
 * file.
 *
 * @author Konstantin Pribluda
 */
public class XStreamContainerBuilder extends ScriptedContainerBuilder  {
    private final Element rootElement;

    private final static String IMPLEMENTATION = "implementation";
    private final static String INSTANCE = "instance";
    private final static String ADAPTER = "adapter";
    private final static String CLASS = "class";
    private final static String KEY = "key";
    private final static String CONSTANT = "constant";
    private final static String DEPENDENCY = "dependency";
    private final static String CONSTRUCTOR = "constructor";

    private final HierarchicalStreamDriver xsdriver;

    /**
     * construct with just reader, use context classloader
     *
     * @param script
     */
    public XStreamContainerBuilder(final Reader script) {
        this(script, Thread.currentThread().getContextClassLoader());
    }

    /**
     * construct with given script and specified classloader
     *
     * @param classLoader
     * @param script
     */
    public XStreamContainerBuilder(final Reader script, final ClassLoader classLoader) {
        this(script, classLoader, new DomDriver());
    }

    public XStreamContainerBuilder(final Reader script, final ClassLoader classLoader, final HierarchicalStreamDriver driver) {
        super(script, classLoader);
        xsdriver = driver;
        InputSource inputSource = new InputSource(script);
        try {
            rootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource)
                    .getDocumentElement();
        } catch (SAXException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        } catch (IOException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        } catch (ParserConfigurationException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        }
    }

    public XStreamContainerBuilder(final URL script, final ClassLoader classLoader, final HierarchicalStreamDriver driver) {
        super(script, classLoader);
        xsdriver = driver;
        try {
            InputSource inputSource = new InputSource(getScriptReader());
            rootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource)
                    .getDocumentElement();
        } catch (SAXException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        } catch (IOException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        } catch (ParserConfigurationException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        }
    }

    public void populateContainer(final MutablePicoContainer container) {
        populateContainer(container, rootElement);
    }

    /**
     * just a convenience method, so we can work recursively with subcontainers
     * for whatever puproses we see cool.
     *
     * @param container
     * @param rootElement
     */
    private void populateContainer(final MutablePicoContainer container, final Element rootElement) {
        NodeList children = rootElement.getChildNodes();
        Node child;
        String name;
        short type;
        for (int i = 0; i < children.getLength(); i++) {
            child = children.item(i);
            type = child.getNodeType();

            if (type == Node.ELEMENT_NODE) {
                name = child.getNodeName();
                if (IMPLEMENTATION.equals(name)) {
                    try {
                        insertImplementation(container, (Element) child);
                    } catch (ClassNotFoundException e) {
                        throw new ScriptedPicoContainerMarkupException(e);
                    }
                } else if (INSTANCE.equals(name)) {
                    insertInstance(container, (Element) child);
                } else if (ADAPTER.equals(name)) {
                    insertAdapter(container, (Element) child);
                } else {
                    throw new ScriptedPicoContainerMarkupException("Unsupported element:" + name);
                }
            }
        }

    }

    /**
     * process adapter node
     *
     * @param container
     * @param rootElement
     */
    @SuppressWarnings("unchecked")
    protected void insertAdapter(final MutablePicoContainer container, final Element rootElement) {
        String key = rootElement.getAttribute(KEY);
        String klass = rootElement.getAttribute(CLASS);
        try {
            DefaultPicoContainer nested = new DefaultPicoContainer();
            populateContainer(nested, rootElement);

            if (key != null) {
                container.addAdapter((ComponentAdapter) nested.getComponent(key));
            } else if (klass != null) {
                Class clazz = getClassLoader().loadClass(klass);
                container.addAdapter((ComponentAdapter) nested.getComponent(clazz));
            } else {
                container.addAdapter(nested.getComponent(ComponentAdapter.class));
            }
        } catch (ClassNotFoundException ex) {
            throw new ScriptedPicoContainerMarkupException(ex);
        }

    }

    /**
     * process implementation node
     *
     * @param container
     * @param rootElement
     * @throws ClassNotFoundException
     */
    protected void insertImplementation(final MutablePicoContainer container, final Element rootElement)
            throws ClassNotFoundException {
        String key = rootElement.getAttribute(KEY);
        String klass = rootElement.getAttribute(CLASS);
        String constructor = rootElement.getAttribute(CONSTRUCTOR);
        if (klass == null || "".equals(klass)) {
            throw new ScriptedPicoContainerMarkupException(
                    "class specification is required for component implementation");
        }

        Class<?> clazz = getClassLoader().loadClass(klass);


        // ok , we processed our children. insert implementation
        Parameter[] parameterArray =  getParameters(rootElement);
        if ("default".equals(constructor)) {
        	parameterArray = Parameter.ZERO;
        }

        NodeList children = rootElement.getChildNodes();
        if (children.getLength() > 0 || "default".equals(constructor)) {
            if (key == null || "".equals(key)) {
                // without key. clazz is our key
                container.addComponent(clazz, clazz, parameterArray);
            } else {
                // with key
                container.addComponent(key, clazz, parameterArray);
            }
        } else {
            if (key == null || "".equals(key)) {
                // without key. clazz is our key
                container.addComponent(clazz, clazz);
            } else {
                // with key
                container.addComponent(key, clazz);
            }

        }
    }

	private Parameter[] getParameters(final Element rootElement) throws ClassNotFoundException {
	    List<Parameter> parameters = new ArrayList<Parameter>();

        NodeList children = rootElement.getChildNodes();
        Node child;
        String name;
        String dependencyKey;
        String dependencyClass;
        Object parseResult;

        for (int i = 0; i < children.getLength(); i++) {
            child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                name = child.getNodeName();
                // constant parameter. it does not have any attributes.
                if (CONSTANT.equals(name)) {
                    // create constant with xstream
                    parseResult = parseElementChild((Element) child);
                    if (parseResult == null) {
                        throw new ScriptedPicoContainerMarkupException("could not parse constant parameter");
                    }
                    parameters.add(new ConstantParameter(parseResult));
                } else if (DEPENDENCY.equals(name)) {
                    // either key or class must be present. not both
                    // key has prececence
                    dependencyKey = ((Element) child).getAttribute(KEY);
                    if (dependencyKey == null || "".equals(dependencyKey)) {
                        dependencyClass = ((Element) child).getAttribute(CLASS);
                        if (dependencyClass == null || "".equals(dependencyClass)) {
                            throw new ScriptedPicoContainerMarkupException(
                                    "either key or class must be present for dependency");
                        } else {
                            parameters.add(new ComponentParameter(getClassLoader().loadClass(dependencyClass)));
                        }
                    } else {
                        parameters.add(new ComponentParameter(dependencyKey));
                    }
                } else if (PARAMETER_ZERO.equals(child.getNodeName())) {
                    	//Check:  We can't check everything here since we aren't schema validating
                    	//But it will at least catch some goofs.
                    	if (!parameters.isEmpty()) {
                    		throw new PicoCompositionException("Cannot mix other parameters with '" + PARAMETER_ZERO +"' nodes.");
                    	}

                    	return Parameter.ZERO;
                }
            }
        }
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    /**
     * process instance node. we get key from atributes (if any ) and leave
     * content to xstream. we allow only one child node inside. (first one wins )
     *
     * @param container
     * @param rootElement
     */
    protected void insertInstance(final MutablePicoContainer container, final Element rootElement) {
        String key = rootElement.getAttribute(KEY);
        Object result = parseElementChild(rootElement);
        if (result == null) {
            throw new ScriptedPicoContainerMarkupException("no content could be parsed in instance");
        }
        if (key != null && !"".equals(key)) {
            // insert with key
            container.addComponent(key, result);
        } else {
            // or without
            container.addComponent(result);
        }
    }

    /**
     * parse element child with xstream and provide object
     *
     * @return
     * @param rootElement
     */
    protected Object parseElementChild(final Element rootElement) {
        NodeList children = rootElement.getChildNodes();
        Node child;
        for (int i = 0; i < children.getLength(); i++) {
            child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return (new XStream(xsdriver)).unmarshal(new DomReader((Element) child));
            }
        }
        return null;
    }

    @Override
	protected PicoContainer createContainerFromScript(final PicoContainer parentContainer, final Object assemblyScope) {
        try {
            // create ComponentInstanceFactory for the container
            MutablePicoContainer childContainer = createMutablePicoContainer(
                     parentContainer, new ContainerOptions(rootElement));
            populateContainer(childContainer);
            return childContainer;
        } catch (PicoClassNotFoundException e) {
            throw new ScriptedPicoContainerMarkupException("Class not found:" + e.getMessage(), e);
        }
    }

    private MutablePicoContainer createMutablePicoContainer(final PicoContainer parentContainer, final ContainerOptions containerOptions) throws PicoCompositionException {
    	boolean caching = containerOptions.isCaching();
    	boolean inherit = containerOptions.isInheritParentBehaviors();
    	String monitorName = containerOptions.getMonitorName();
    	String componentFactoryName = containerOptions.getComponentFactoryName();

    	if (inherit) {
    		if (!(parentContainer instanceof MutablePicoContainer)) {
    			throw new PicoCompositionException("For behavior inheritance to be used, the parent picocontainer must be of type MutablePicoContainer");
    		}

    		MutablePicoContainer parentPico = (MutablePicoContainer)parentContainer;
    		return parentPico.makeChildContainer();
    	}

    	ScriptedBuilder builder = new ScriptedBuilder(parentContainer);
        if (caching) {
			builder.withCaching();
		}
        return builder
            .withClassLoader(getClassLoader())
            .withLifecycle()
            .withComponentFactory(componentFactoryName)
            .withMonitor(monitorName)
            .build();

    }

}
