/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web.struts2;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import ognl.OgnlRuntime;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.web.PicoServletContainerListener;
import com.picocontainer.web.ScopedContainers;
import com.picocontainer.web.providers.AbstractScopedContainerBuilder;
import com.picocontainer.web.providers.PicoServletParameterProcessor;

@SuppressWarnings("serial")
public class Struts2PicoServletContainerListener extends PicoServletContainerListener {

    public void contextInitialized(ServletContextEvent event) {
        OgnlRuntime.setSecurityManager(null);
        super.contextInitialized(event);
    }

    @Override
    protected ScopedContainers makeScopedContainers(ServletContext servletContext) {
    	/*
        DefaultPicoContainer appCtnr = new DefaultPicoContainer(makeParentContainer(), makeLifecycleStrategy(), makeAppComponentMonitor(), new Guarding().wrap(new Caching()));
        Storing sessStoring;
        ThreadLocalLifecycleState sessionState;
        DefaultPicoContainer sessCtnr;
        PicoContainer parentOfRequestContainer;
        if (stateless) {
        	  sessionState = null;
              sessStoring = null;
              sessCtnr = null;
              parentOfRequestContainer = appCtnr;
        } else {
        	sessStoring = new Storing();
        	sessionState = new ThreadLocalLifecycleState();
        	sessCtnr = new DefaultPicoContainer(appCtnr, makeLifecycleStrategy(), makeSessionComponentMonitor(), new Guarding().wrap(sessStoring));
        	parentOfRequestContainer = sessCtnr;
            sessCtnr.setLifecycleState(sessionState);
        }
        
        ThreadLocalLifecycleState requestState = new ThreadLocalLifecycleState();
        Storing reqStoring = new Storing();
        DefaultPicoContainer reqCtnr = new DefaultPicoContainer(parentOfRequestContainer, makeLifecycleStrategy(), makeRequestComponentMonitor(), new Guarding().wrap(addRequestBehaviors(reqStoring)));
        reqCtnr.setLifecycleState(requestState);

        return new ScopedContainers(appCtnr, sessCtnr, reqCtnr, sessStoring, reqStoring, sessionState, requestState);
        */
    	
		PicoServletParameterProcessor paramProcessor = new PicoServletParameterProcessor();
		AbstractScopedContainerBuilder containerBuilder = paramProcessor.processContextParameters(servletContext);
		ScopedContainers containers =  containerBuilder.makeScopedContainers(paramProcessor.isStateless());
    	    	

		return containers;
    }

    /**
     * Struts2 handles whole value objects in some configurations.
     * This enables lazy instantiation of them    
     */
    @Override
    protected ComponentMonitor makeRequestComponentMonitor() {
        return new StrutsActionInstantiatingComponentMonitor();
    }
}
