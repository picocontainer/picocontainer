/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/

package com.picocontainer.web.struts2;

import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import com.picocontainer.web.AbstractPicoServletContainerFilter;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.entities.InterceptorConfig;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.picocontainer.MutablePicoContainer;

/**
 * XWork2 ObjectFactory implementation to delegate action/component/bean lookups
 * to PicoContainer.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class PicoObjectFactory extends ObjectFactory {
	
    private static ThreadLocal<MutablePicoContainer> currentRequestContainer;
    private static ThreadLocal<MutablePicoContainer> currentSessionContainer;
    private static ThreadLocal<MutablePicoContainer> currentAppContainer;

    public static class ServletFilter extends AbstractPicoServletContainerFilter {
		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
			synchronized(PicoObjectFactory.class) {
				if (currentAppContainer == null) {
					currentAppContainer = new ThreadLocal<MutablePicoContainer>();
				}
				
				if (currentSessionContainer == null) {
					currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
				}
				if (currentRequestContainer == null) {
					currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
				}
			}
			super.init(filterConfig);
		}    	
    	
        protected synchronized void setAppContainer(MutablePicoContainer container) {
        	if (currentAppContainer == null) {
        		currentAppContainer = new ThreadLocal<MutablePicoContainer>();
        	}
            currentAppContainer.set(container);
        }
        protected synchronized void setRequestContainer(MutablePicoContainer container) {
        	if (currentRequestContainer == null) {
        		currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
        	}
            currentRequestContainer.set(container);
        }
        protected synchronized void setSessionContainer(MutablePicoContainer container) {
        	if (currentSessionContainer == null) {
        		currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
        	}
            currentSessionContainer.set(container);
        }
        
		public void destroy() {
			if (currentRequestContainer != null) {
				currentRequestContainer.remove();
				currentRequestContainer = null;
			}
			
			if (currentSessionContainer != null) {
				currentSessionContainer.remove();
				currentSessionContainer = null;
			}
			
			if (currentAppContainer != null) {
				currentAppContainer.remove();
				currentAppContainer = null;
			}
			
		}
		@Override
		protected final MutablePicoContainer getRequestContainer() {
			if (currentRequestContainer == null || currentRequestContainer.get() == null) {
				throw newListenerNotInstalledRightIllegalStateException();
			}
			return currentRequestContainer.get();
		}

    }

    @SuppressWarnings({ "rawtypes" })
    public Class getClassInstance(String name) throws ClassNotFoundException {
        Class clazz = super.getClassInstance(name);
        synchronized (this) {
        	MutablePicoContainer reqContainer = currentRequestContainer != null ? currentRequestContainer.get() : null;
            if (reqContainer != null) {
                // forces a registration via noComponentFound()
                reqContainer.getComponentAdapter(clazz);
            }
        }
        return clazz;
    }

	
	private MutablePicoContainer getAppContainer() {
		if (currentAppContainer == null || currentAppContainer.get() == null) {
			throw newListenerNotInstalledRightIllegalStateException();
		}
		return currentAppContainer.get();		
	}
	
	
	private static IllegalStateException newListenerNotInstalledRightIllegalStateException() {
		return new IllegalStateException("Container have not been set up correctly.  Is " 
				+ Struts2PicoServletContainerListener.class.getName() 
				+ " properly installed in your web.xml (and before struts filters)?");
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object buildBean(Class clazz, Map extraContext) throws Exception {

        MutablePicoContainer requestContainer = currentRequestContainer != null ? currentRequestContainer.get() : null;
        if (requestContainer == null) {
            MutablePicoContainer appContainer = getAppContainer();
            Object comp = appContainer.getComponent(clazz);
            if (comp == null) {
                appContainer.addComponent(clazz);
                comp = appContainer.getComponent(clazz);
            }
            return comp;

        }
        return requestContainer.getComponent(clazz);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Interceptor buildInterceptor(InterceptorConfig config, Map params) throws ConfigurationException {
        return super.buildInterceptor(config, params);
    }

    public boolean isNoArgConstructorRequired() {
        return false;
    }

}
