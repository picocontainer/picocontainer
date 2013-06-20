/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.webwork;

import java.util.HashMap;
import java.util.Map;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.web.PicoServletFilter;

import webwork.action.Action;
import webwork.action.factory.ActionFactory;

/**
 * Replacement for the standard WebWork JavaActionFactory that uses a
 * PicoContainer to resolve all of the dependencies an Action may have.
 * 
 * @author Joe Walnes
 * @author Mauro Talevi
 */
public final class PicoActionFactory extends ActionFactory {

	private PicoHook picoHook = new PicoHook();

    @SuppressWarnings("serial")
    private static class PicoHook extends PicoServletFilter {
    	
    	protected MutablePicoContainer getRequestContainerForThread() {
    		return super.getRequestContainer();
    	}
    }

    private final Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();

    public Action getActionImpl(String className) {
        try {
            Class<?> actionClass = getActionClass(className);
            Action action = null;
            try {
                action = instantiateAction(actionClass);
            } catch (Exception e) {
                // swallow these exceptions and return null action
            }
            return action;
        } catch (PicoCompositionException e) {
            return null;
        }
    }

    protected Action instantiateAction(Class<?> actionClass) {
        MutablePicoContainer actionsContainer =  picoHook.getRequestContainerForThread();
        Action action = (Action) actionsContainer.getComponent(actionClass);

        if (action == null) {
            // The action wasn't registered. Attempt to instantiate it.
            actionsContainer.addComponent(actionClass);
            action = (Action) actionsContainer.getComponent(actionClass);
        }
        return action;
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
