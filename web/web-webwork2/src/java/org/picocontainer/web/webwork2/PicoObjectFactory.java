/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.webwork2;

import java.util.HashMap;
import java.util.Map;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.web.PicoServletContainerFilter;

import com.opensymphony.xwork.ObjectFactory;

/**
 * <p>
 * XWork ObjectFactory which uses a PicoContainer to create component instances.
 * </p>
 * 
 * @author Cyrille Le Clerc
 * @author Jonas Engman
 * @author Mauro Talevi
 * @author Gr&eacute;gory Joseph
 * @author Konstatin Pribluda 
 */
public class PicoObjectFactory extends ObjectFactory {

    private static ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
    private static ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
    private static ThreadLocal<MutablePicoContainer> currentAppContainer = new ThreadLocal<MutablePicoContainer>();

    @SuppressWarnings("serial")
    public static class ServletFilter extends PicoServletContainerFilter {
        protected void setAppContainer(MutablePicoContainer container) {
            currentAppContainer.set(container);
        }
        protected void setRequestContainer(MutablePicoContainer container) {
            currentRequestContainer.set(container);
        }
        protected void setSessionContainer(MutablePicoContainer container) {
            currentSessionContainer.set(container);
        }
    }

    private final Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();

    public boolean isNoArgConstructorRequired() {
        return false;
    }

    @SuppressWarnings("unchecked")
    public Object buildBean(Class clazz, Map extraContext) throws Exception {
        return buildBean(clazz);
    }

    @SuppressWarnings("unchecked")
    public Object buildBean(String className, Map extraContext) throws Exception {
        return buildBean(className);
    }


    @SuppressWarnings("unchecked")
    public Class getClassInstance(String className) {
        return getActionClass(className);
    }

    /**
     * Instantiates an action using the PicoContainer found in the request scope.
     * if action or bean is not registered explicitely, new instance will be provided
     * on every invocation.
     * 
     * @see com.opensymphony.xwork.ObjectFactory#buildBean(java.lang.Class)
     */
    public Object buildBean(Class<?> actionClass) throws Exception {
        PicoContainer actionsContainer = currentRequestContainer.get();
        Object action = actionsContainer.getComponent(actionClass);

        if (action == null) {
            // The action wasn't registered. Attempt to instantiate it.
        	// use child container to prevent weirdest errors
        	MutablePicoContainer child = new DefaultPicoContainer(actionsContainer);
        	
            child.addComponent(actionClass);
            action = child.getComponent(actionClass);
        }
        return action;
    }

    /**
     * As {@link ObjectFactory#buildBean(java.lang.String)}does not delegate to
     * {@link ObjectFactory#buildBean(java.lang.Class)} but directly calls
     * <code>clazz.newInstance()</code>, overwrite this method to call
     * <code>buildBean()</code>
     *
     * @see com.opensymphony.xwork.ObjectFactory#buildBean(java.lang.String)
     */
    public Object buildBean(String className) throws Exception {
        return buildBean(getActionClass(className));
    }

    public Class<?> getActionClass(String className) throws PicoCompositionException {
        try {
            return loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new PicoCompositionException("Action class '" + className + "' not found", e);
        }
    }

    protected Class<?> loadClass(String className) throws ClassNotFoundException {
        if (classCache.containsKey(className)) {
            return (Class<?>) classCache.get(className);
        } else {
            Class<?> result = Thread.currentThread().getContextClassLoader().loadClass(className);
            classCache.put(className, result);
            return result;
        }
    }  

}
