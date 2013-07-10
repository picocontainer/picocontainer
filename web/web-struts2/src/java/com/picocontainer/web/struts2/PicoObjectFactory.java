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
import com.picocontainer.web.PicoServletFilter;

import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.entities.InterceptorConfig;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;

/**
 * XWork2 ObjectFactory implementation to delegate action/component/bean lookups
 * to PicoContainer.
 * 
 * @author Paul Hammant
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class PicoObjectFactory extends ObjectFactory {
	
	/*
    private static ThreadLocal<MutablePicoContainer> currentRequestContainer;
    private static ThreadLocal<MutablePicoContainer> currentSessionContainer;
    private static ThreadLocal<MutablePicoContainer> currentAppContainer;
*/
	
	private final PicoHook picoHook = new PicoHook();
	
	
    private  class PicoHook extends PicoServletFilter {
		public PicoContainer getCurrentRequestPico() {
			return super.getRequestContainerWithoutException();
		}
		
		public MutablePicoContainer getAppContainer() {
			return super.getApplicationContainerWithoutException();
		}
    }
    

    @SuppressWarnings({ "rawtypes" })
    public Class getClassInstance(String name) throws ClassNotFoundException {
        Class clazz = super.getClassInstance(name);
        synchronized (this) {
        	PicoContainer reqContainer = picoHook.getCurrentRequestPico();
            if (reqContainer != null) {
                // forces a registration via noComponentFound()
                reqContainer.getComponentAdapter(clazz);
            }
        }
        return clazz;
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object buildBean(Class clazz, Map extraContext) throws Exception {

        PicoContainer requestContainer = picoHook.getCurrentRequestPico();
        if (requestContainer == null) {
            MutablePicoContainer appContainer = picoHook.getAppContainer();
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
