/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.script.xml;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.lang.reflect.Type;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentFactory;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoClassNotFoundException;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.Injector;
import org.picocontainer.BehaviorFactory;
import org.picocontainer.injectors.ConstructorInjection;
import org.picocontainer.injectors.AbstractInjectionFactory;
import org.picocontainer.injectors.MultiArgMemberInjector;
import org.picocontainer.classname.ClassPathElement;
import org.picocontainer.classname.ClassLoadingPicoContainer;
import org.picocontainer.classname.DefaultClassLoadingPicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.picocontainer.monitors.NullComponentMonitor;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.classname.ClassName;
import org.picocontainer.script.LifecycleMode;
import org.picocontainer.script.ScriptedBuilder;
import org.picocontainer.script.ScriptedContainerBuilder;
import org.picocontainer.script.ScriptedPicoContainerMarkupException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import static org.picocontainer.script.xml.AttributeUtils.*;
import static org.picocontainer.script.xml.XMLConstants.*;

/**
 * This class builds up a hierarchy of PicoContainers from an XML configuration file.
 *
 * @author Paul Hammant
 * @author Aslak Helles&oslash;y
 * @author Jeppe Cramon
 * @author Mauro Talevi
 */
public class XMLContainerBuilder extends ScriptedContainerBuilder {

    private final static String DEFAULT_COMPONENT_INSTANCE_FACTORY = BeanComponentInstanceFactory.class.getName();



    private Element rootElement;
    
    
    /**
     * The XMLComponentInstanceFactory globally defined for the container.
     * It may be overridden at node level.
     */
    private XMLComponentInstanceFactory componentInstanceFactory;

    public XMLContainerBuilder(Reader script, ClassLoader classLoader) {
    	this(script,classLoader, LifecycleMode.AUTO_LIFECYCLE);
    }
    
    public XMLContainerBuilder(Reader script, ClassLoader classLoader, LifecycleMode lifecycleMode) {
        super(script, classLoader, lifecycleMode);
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            parse(documentBuilder, new InputSource(script));
        } catch (ParserConfigurationException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        }
    }

    public XMLContainerBuilder(final URL script, ClassLoader classLoader) {
    	this(script,classLoader, LifecycleMode.AUTO_LIFECYCLE);
    }
    
    public XMLContainerBuilder(final URL script, ClassLoader classLoader, LifecycleMode lifecycleMode) {
        super(script, classLoader, lifecycleMode);
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            documentBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws IOException {
                    URL url = new URL(script, systemId);
                    return new InputSource(url.openStream());
                }
            });
            parse(documentBuilder, new InputSource(script.toString()));
        } catch (ParserConfigurationException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        }
    }

    private void parse(DocumentBuilder documentBuilder, InputSource inputSource) {
        try {
            rootElement = documentBuilder.parse(inputSource).getDocumentElement();
        } catch (SAXException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        } catch (IOException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        }
    }

    protected PicoContainer createContainerFromScript(PicoContainer parentContainer, Object assemblyScope) {
        try {
            // create ComponentInstanceFactory for the container
            componentInstanceFactory = createComponentInstanceFactory(rootElement.getAttribute(COMPONENT_INSTANCE_FACTORY));
            MutablePicoContainer childContainer = createMutablePicoContainer(
                     parentContainer, new ContainerOptions(rootElement));
            populateContainer(childContainer);
            return childContainer;
        } catch (PicoClassNotFoundException e) {
            throw new ScriptedPicoContainerMarkupException("Class not found:" + e.getMessage(), e);
        }
    }

    private MutablePicoContainer createMutablePicoContainer(PicoContainer parentContainer, ContainerOptions containerOptions) throws PicoCompositionException {
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
        if (caching) builder.withCaching();
        return builder
            .withClassLoader(getClassLoader())
            .withLifecycle()
            .withComponentFactory(componentFactoryName)
            .withMonitor(monitorName)
            .build();

    }

    public void populateContainer(MutablePicoContainer container) {
        try {
            String parentClass = rootElement.getAttribute("parentclassloader");
            ClassLoader classLoader = getClassLoader();
            if (parentClass != null && !EMPTY.equals(parentClass)) {
                classLoader = classLoader.loadClass(parentClass).getClassLoader();
            }
            ClassLoadingPicoContainer scriptedContainer = new DefaultClassLoadingPicoContainer(classLoader, container);
            ClassLoadingPicoContainer classLoadingPicoContainer = new DefaultClassLoadingPicoContainer(getClassLoader());
            addComponentsAndChildContainers(scriptedContainer, rootElement, classLoadingPicoContainer);
        } catch (ClassNotFoundException e) {
            throw new ScriptedPicoContainerMarkupException("Class not found: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        } catch (SAXException e) {
            throw new ScriptedPicoContainerMarkupException(e);
        }
    }

    private void addComponentsAndChildContainers(ClassLoadingPicoContainer parentContainer, Element containerElement, ClassLoadingPicoContainer knownComponentAdapterFactories) throws ClassNotFoundException, IOException, SAXException {

        ClassLoadingPicoContainer metaContainer = new DefaultClassLoadingPicoContainer(getClassLoader(),
                new CompFactoryWrappingComponentFactory(), knownComponentAdapterFactories);
        NodeList children = containerElement.getChildNodes();
        // register classpath first, regardless of order in the document.
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);
                String name = childElement.getNodeName();
                if (CLASSPATH.equals(name)) {
                    addClasspath(parentContainer, childElement);
                }
            }
        }
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);
                String name = childElement.getNodeName();
                if (CONTAINER.equals(name)) {
                    MutablePicoContainer childContainer = parentContainer.makeChildContainer();
                    ClassLoadingPicoContainer childPicoContainer = new DefaultClassLoadingPicoContainer(parentContainer.getComponentClassLoader(), childContainer);
                    addComponentsAndChildContainers(childPicoContainer, childElement, metaContainer);
                } else if (COMPONENT_IMPLEMENTATION.equals(name)
                        || COMPONENT.equals(name)) {
                    addComponent(parentContainer, childElement, new Properties[0]);
                } else if (COMPONENT_INSTANCE.equals(name)) {
                    registerComponentInstance(parentContainer, childElement);
                } else if (COMPONENT_ADAPTER.equals(name)) {
                    addComponentAdapter(parentContainer, childElement, metaContainer);
                } else if (COMPONENT_ADAPTER_FACTORY.equals(name)) {
                    addComponentFactory(childElement, metaContainer);
                } else if (CLASSLOADER.equals(name)) {
                    addClassLoader(parentContainer, childElement, metaContainer);
                } else if (!CLASSPATH.equals(name)) {
                    throw new ScriptedPicoContainerMarkupException("Unsupported element:" + name);
                }
            }
        }
    }


    private void addComponentFactory(Element element, ClassLoadingPicoContainer metaContainer) throws MalformedURLException, ClassNotFoundException {
        if (notSet(element.getAttribute(KEY))) {
            throw new ScriptedPicoContainerMarkupException("'" + KEY + "' attribute not specified for " + element.getNodeName());
        }
        Element node = (Element)element.cloneNode(false);
        NodeList children = element.getChildNodes();
        String key = null;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);
                String name = childElement.getNodeName();
                if (COMPONENT_ADAPTER_FACTORY.equals(name)) {
                    if (!"".equals(childElement.getAttribute(KEY))) {
                        throw new ScriptedPicoContainerMarkupException("'" + KEY + "' attribute must not be specified for nested " + element.getNodeName());
                    }
                    childElement = (Element)childElement.cloneNode(true);
                    key = "ContrivedKey:" + String.valueOf(System.identityHashCode(childElement));
                    childElement.setAttribute(KEY, key);
                    addComponentFactory(childElement, metaContainer);
                    // replace nested CAF with a ComponentParameter using an internally generated key
                    //Element parameter = node.getOwnerDocument().createElement(PARAMETER);
                    //parameter.setAttribute(KEY, key);
                    //node.appendChild(parameter);
                } else if (PARAMETER.equals(name)) {
                    node.appendChild(childElement.cloneNode(true));
                }
            }
        }
        // handle CAF now as standard component in the metaContainer
        if (key != null) {
            addComponent(metaContainer, node, new ForCaf(key));
        } else {
            addComponent(metaContainer, node, new ForCaf[0]);
        }
    }

    @SuppressWarnings("serial")
    public class ForCaf extends Properties {

        public ForCaf(String key) {
            super.put("ForCAF", key);
        }
    }

    private void addClassLoader(ClassLoadingPicoContainer parentContainer, Element childElement, ClassLoadingPicoContainer metaContainer) throws IOException, SAXException, ClassNotFoundException {
        String parentClass = childElement.getAttribute("parentclassloader");
        ClassLoader parentClassLoader = parentContainer.getComponentClassLoader();
        if (parentClass != null && !EMPTY.equals(parentClass)) {
            parentClassLoader = parentClassLoader.loadClass(parentClass).getClassLoader();
        }
        ClassLoadingPicoContainer scripted = new DefaultClassLoadingPicoContainer(parentClassLoader, parentContainer);
        addComponentsAndChildContainers(scripted, childElement, metaContainer);
    }

    private void addClasspath(ClassLoadingPicoContainer container, Element classpathElement) throws IOException, ClassNotFoundException {
        NodeList children = classpathElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);

                String fileName = childElement.getAttribute(FILE);
                String urlSpec = childElement.getAttribute(URL);
                URL url;
                if (urlSpec != null && !EMPTY.equals(urlSpec)) {
                    url = new URL(urlSpec);
                } else {
                    File file = new File(fileName);
                    if (!file.exists()) {
                        throw new IOException(file.getAbsolutePath() + " doesn't exist");
                    }
                    url = file.toURL();
                }
                ClassPathElement cpe = container.addClassLoaderURL(url);
                addPermissions(cpe, childElement);
            }
        }
    }

    private void addPermissions(ClassPathElement classPathElement, Element classPathXmlElement) throws ClassNotFoundException {
        NodeList children = classPathXmlElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);

                String permissionClassName = childElement.getAttribute(CLASSNAME);
                String action = childElement.getAttribute(CONTEXT);
                String value = childElement.getAttribute(VALUE);
                MutablePicoContainer mpc = new DefaultPicoContainer();
                mpc.addComponent(Permission.class, Class.forName(permissionClassName), new ConstantParameter(action), new ConstantParameter(value));

                Permission permission = mpc.getComponent(Permission.class);
                classPathElement.grantPermission(permission);
            }
        }

    }

    private void addComponent(ClassLoadingPicoContainer container, Element element, Properties... props) throws ClassNotFoundException, MalformedURLException {
        String className = element.getAttribute(CLASS);
        if (notSet(className)) {
            throw new ScriptedPicoContainerMarkupException("'" + CLASS + "' attribute not specified for " + element.getNodeName());
        }

        Parameter[] parameters = createChildParameters(container, element);
        Class<?> clazz = container.getComponentClassLoader().loadClass(className);
        Object key = element.getAttribute(KEY);
        if (notSet(key)) {
            String classKey = element.getAttribute(CLASS_NAME_KEY);
            if (isSet(classKey)) {
                key = getClassLoader().loadClass(classKey);
            } else {
                key = clazz;
            }
        }
        if (parameters == null) {
            container.addComponent(key, clazz);
        } else {
            container.as(props).addComponent(key, clazz, parameters);
        }
    }



    private Parameter[] createChildParameters(ClassLoadingPicoContainer container, Element element) throws ClassNotFoundException, MalformedURLException {
        List<Parameter> parametersList = new ArrayList<Parameter>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element childElement = (Element) children.item(i);
                if (PARAMETER.equals(childElement.getNodeName())) {
                    parametersList.add(createParameter(container, childElement));
                }
                
                if (PARAMETER_ZERO.equals(childElement.getNodeName())) {
                	//Check:  We can't check everything here since we aren't schema validating
                	//But it will at least catch some goofs.
                	if (!parametersList.isEmpty()) {
                		throw new PicoCompositionException("Cannot mix other parameters with '" + PARAMETER_ZERO +"' nodes." );
                	}
                	
                	return Parameter.ZERO;
                }
            }
        }

        Parameter[] parameters = null;
        if (!parametersList.isEmpty()) {
            parameters = parametersList.toArray(new Parameter[parametersList.size()]);
        }
        return parameters;
    }

    /**
     * Build the org.picocontainer.Parameter from the <code>parameter</code> element. This could
     * create either a ComponentParameter or ConstantParameter instance,
     * depending on the values of the element's attributes. This is somewhat
     * complex because there are five constructors for ComponentParameter and one for 
     * ConstantParameter. These are:
     * 
     * <a href="http://www.picocontainer.org/picocontainer/latest/picocontainer/apidocs/org/picocontainer/defaults/ComponentParameter.html">ComponentParameter Javadocs</a>:
     * 
     * <code>ComponentParameter() - Expect any scalar paramter of the appropriate type or an Array.
     *       ComponentParameter(boolean emptyCollection) - Expect any scalar paramter of the appropriate type or an Array.
     *       ComponentParameter(Class componentValueType, boolean emptyCollection) - Expect any scalar paramter of the appropriate type or the collecting type Array,Collectionor Map.
     *       ComponentParameter(Class componentKeyType, Class componentValueType, boolean emptyCollection) - Expect any scalar paramter of the appropriate type or the collecting type Array,Collectionor Map.
     *       ComponentParameter(Object componentKey) - Expect a parameter matching a component of a specific key.</code>
     * 
     * and
     * 
     * <a href="http://www.picocontainer.org/picocontainer/latest/picocontainer/apidocs/org/picocontainer/defaults/ConstantParameter.html">ConstantParameter Javadocs</a>:
     * 
     * <code>ConstantParameter(Object value)</code>
     * 
     * The rules for this are, in order:
     * 
     * 1) If the <code>key</code> attribute is not null/empty, the fifth constructor will be used.
     * 2) If the <code>componentKeyType</code> attribute is not null/empty, the fourth constructor will be used.  
     *    In this case, both the <code>componentValueType</code> and <code>emptyCollection</code> attributes must be non-null/empty or an exception will be thrown.
     * 3) If the <code>componentValueType</code> attribute is not null/empty, the third constructor will be used.
     *    In this case, the <code>emptyCollection</code> attribute must be non-null/empty.
     * 4) If the <code>emptyCollection</code> attribute is not null/empty, the second constructor will be used.
     * 5) If there is no child element of the parameter, the first constructor will be used.
     * 6) Otherwise, the return value will be a ConstantParameter with the return from the createInstance value.
     * @param element
     * @param pico
     * @return
     * @throws ClassNotFoundException
     * @throws MalformedURLException
     */
    private Parameter createParameter(PicoContainer pico, Element element) throws ClassNotFoundException, MalformedURLException {
        final Parameter parameter;
        String key = element.getAttribute(KEY);
        String emptyCollectionString = element.getAttribute(EMPTY_COLLECTION);
        String componentValueTypeString = element.getAttribute(COMPONENT_VALUE_TYPE);
        String componentKeyTypeString = element.getAttribute(COMPONENT_KEY_TYPE);

        // key not null/empty takes precidence 
        if (key != null && !EMPTY.equals(key)) {
            parameter = new ComponentParameter(key);
        } else if (componentKeyTypeString != null && !EMPTY.equals(componentKeyTypeString)) {
            if (emptyCollectionString == null || componentValueTypeString == null || 
                    EMPTY.equals(emptyCollectionString) || EMPTY.equals(componentValueTypeString)) {
                
                throw new ScriptedPicoContainerMarkupException("The componentKeyType attribute was specified (" +
                        componentKeyTypeString + ") but one or both of the emptyCollection (" + 
                        emptyCollectionString + ") or componentValueType (" + componentValueTypeString + 
                        ") was empty or null.");
            }
            
            Class<?> componentKeyType = getClassLoader().loadClass(componentKeyTypeString);
            Class<?> componentValueType = getClassLoader().loadClass(componentValueTypeString);
            
            boolean emptyCollection = Boolean.valueOf(emptyCollectionString);
            
            parameter = new ComponentParameter(componentKeyType, componentValueType, emptyCollection);
        } else if (componentValueTypeString != null && !EMPTY.equals(componentValueTypeString)) {
            if (emptyCollectionString == null || EMPTY.equals(emptyCollectionString)) {
                
                throw new ScriptedPicoContainerMarkupException("The componentValueType attribute was specified (" +
                        componentValueTypeString + ") but the emptyCollection (" + 
                        emptyCollectionString + ") was empty or null.");
            }
            
            Class<?> componentValueType = getClassLoader().loadClass(componentValueTypeString);
            
            boolean emptyCollection = Boolean.valueOf(emptyCollectionString);
            
            parameter = new ComponentParameter(componentValueType, emptyCollection);
        } else if (emptyCollectionString != null && !EMPTY.equals(emptyCollectionString)) {
            boolean emptyCollection = Boolean.valueOf(emptyCollectionString);
            
            parameter = new ComponentParameter(emptyCollection);
        }
        else if (getFirstChildElement(element, false) == null) {
            parameter = new ComponentParameter();
        } else {
            Object instance = createInstance(pico, element);
            parameter = new ConstantParameter(instance);
        }
        return parameter;
    }


    private void registerComponentInstance(ClassLoadingPicoContainer container, Element element) throws ClassNotFoundException, PicoCompositionException, MalformedURLException {
        Object instance = createInstance(container, element);
        String key = element.getAttribute(KEY);
        String classKey = element.getAttribute(CLASS_NAME_KEY);
        if (notSet(key)) {
            if (!notSet(classKey)) {
                container.addComponent(getClassLoader().loadClass(classKey), instance);
            } else {
                container.addComponent(instance);
            }
        } else {
            container.addComponent(key, instance);
        }
    }

    private Object createInstance(PicoContainer pico, Element element) throws MalformedURLException {
        XMLComponentInstanceFactory factory = createComponentInstanceFactory(element.getAttribute(FACTORY));
        Element instanceElement = getFirstChildElement(element, true);
        return factory.makeInstance(pico, instanceElement, getClassLoader());
    }

    private Element getFirstChildElement(Element parent, boolean fail) {
        NodeList children = parent.getChildNodes();
        Element child = null;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                child = (Element) children.item(i);
                break;
            }
        }
        if (child == null && fail) {
            throw new ScriptedPicoContainerMarkupException(parent.getNodeName() + " needs a child element");
        }
        return child;
    }

    private XMLComponentInstanceFactory createComponentInstanceFactory(String factoryClass) {
        if ( notSet(factoryClass)) {
            // no factory has been specified for the node
            // return globally defined factory for the container - if there is one
            if (componentInstanceFactory != null) {
                return componentInstanceFactory;
            }
            factoryClass = DEFAULT_COMPONENT_INSTANCE_FACTORY;
        }

        // using a PicoContainer is overkill here.
        try {
            return (XMLComponentInstanceFactory)getClassLoader().loadClass(factoryClass).newInstance();
        } catch (InstantiationException e) {
            throw new PicoCompositionException(e);
        } catch (IllegalAccessException e) {
            throw new PicoCompositionException(e);
        } catch (ClassNotFoundException e) {
            throw new PicoClassNotFoundException(factoryClass, e);
        }
    }

    private void addComponentAdapter(ClassLoadingPicoContainer container, Element element, ClassLoadingPicoContainer metaContainer) throws ClassNotFoundException, PicoCompositionException, MalformedURLException {
        String className = element.getAttribute(CLASS);
        if (notSet(className)) {
            throw new ScriptedPicoContainerMarkupException("'" + CLASS + "' attribute not specified for " + element.getNodeName());
        }
        Class<?> implementationClass = getClassLoader().loadClass(className);
        Object key = element.getAttribute(KEY);
        String classKey = element.getAttribute(CLASS_NAME_KEY);
        if (notSet(key)) {
            if (!notSet(classKey)) {
                key = getClassLoader().loadClass(classKey);
            } else {
                key = implementationClass;
            }
        }
        Parameter[] parameters = createChildParameters(container, element);
        ComponentFactory componentFactory = createComponentFactory(element.getAttribute(FACTORY), metaContainer);

        container.as(Characteristics.NONE).addAdapter(componentFactory.createComponentAdapter(new NullComponentMonitor(), new NullLifecycleStrategy(), new Properties(), key, implementationClass, parameters));
    }

    private ComponentFactory createComponentFactory(String factoryName, ClassLoadingPicoContainer metaContainer) throws PicoCompositionException {
        if ( notSet(factoryName)) {
            return new Caching().wrap(new ConstructorInjection());
        }
        final Serializable key;
        if (metaContainer.getComponentAdapter(factoryName) != null) {
            key = factoryName;
        } else {
            metaContainer.addComponent(ComponentFactory.class, new ClassName(factoryName));
            key = ComponentFactory.class;
        }
        return (ComponentFactory) metaContainer.getComponent(key);
    }


    @SuppressWarnings({"serial","synthetic-access"})
    public static class CompFactoryWrappingComponentFactory extends AbstractInjectionFactory {

        ConstructorInjection constructorInjection = new ConstructorInjection();

        public <T> ComponentAdapter<T> createComponentAdapter(ComponentMonitor monitor, LifecycleStrategy lifecycle, Properties props, Object key, Class<T> impl, Parameter... parms)
                throws PicoCompositionException {

            ComponentAdapter<T> adapter = constructorInjection.createComponentAdapter(monitor, lifecycle, props, key, impl, parms);
            String otherKey = props.getProperty("ForCAF");
            if (otherKey != null && !otherKey.equals("")) {
                props.remove("ForCAF");
                return new MySingleMemberInjector(key, impl, parms, monitor, false, otherKey, (Injector) adapter);
            }
            return adapter;
        }
    }

    @SuppressWarnings("serial")
    private static class MySingleMemberInjector extends MultiArgMemberInjector {
        private final String otherKey;
        private final Injector injector;

        private MySingleMemberInjector(Object key, Class impl, Parameter[] parms,
                                       ComponentMonitor monitor, 
                                       boolean useNames, String otherKey, Injector injector) {
            super(key, impl, parms, monitor, useNames);
            this.otherKey = otherKey;
            this.injector = injector;
        }

        @Override
        public Object getComponentInstance(PicoContainer container, Type into) throws PicoCompositionException {
            BehaviorFactory bf = (BehaviorFactory) injector.getComponentInstance(container, into);
            bf.wrap((ComponentFactory) container.getComponent(otherKey));
            return bf;
        }
    }
}
