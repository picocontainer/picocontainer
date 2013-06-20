/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/

package org.picocontainer.web.struts2;

import java.util.Map;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.web.AbstractPicoServletContainerFilter;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.entities.InterceptorConfig;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * XWork2 ObjectFactory implementation to delegate action/component/bean lookups
 * to PicoContainer.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class PicoObjectFactory extends ObjectFactory {
	
    private static ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
    private static ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
    private static ThreadLocal<MutablePicoContainer> currentAppContainer = new ThreadLocal<MutablePicoContainer>();

    public static class ServletFilter extends AbstractPicoServletContainerFilter {
        protected void setAppContainer(MutablePicoContainer container) {
            currentAppContainer.set(container);
        }
        protected void setRequestContainer(MutablePicoContainer container) {
            currentRequestContainer.set(container);
        }
        protected void setSessionContainer(MutablePicoContainer container) {
            currentSessionContainer.set(container);
        }
        
		public void destroy() {
			// TODO Auto-generated method stub
			
		}
		@Override
		protected final MutablePicoContainer getRequestContainer() {
			if (currentRequestContainer == null || currentRequestContainer.get() == null) {
				throw new IllegalStateException("currentRequestContainer has not yet been set.  Is " + ServletFilter.class.getName() + " properly installed in your web.xml?");
			}
			return currentRequestContainer.get();
		}
    }

    @SuppressWarnings({ "rawtypes" })
    public Class getClassInstance(String name) throws ClassNotFoundException {
        Class clazz = super.getClassInstance(name);
        synchronized (this) {
            MutablePicoContainer reqContainer = currentRequestContainer.get();
            if (reqContainer != null) {
                // forces a registration via noComponentFound()
                reqContainer.getComponentAdapter(clazz);
            }
        }
        return clazz;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object buildBean(Class clazz, Map extraContext) throws Exception {

        MutablePicoContainer requestContainer = currentRequestContainer.get();
        if (requestContainer == null) {
            MutablePicoContainer appContainer = currentAppContainer.get();
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
