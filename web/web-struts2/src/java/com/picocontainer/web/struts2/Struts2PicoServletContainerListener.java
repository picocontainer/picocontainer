/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web.struts2;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.Result;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.DefaultPicoContainer;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoCompositionException;
import com.picocontainer.PicoContainer;
import com.picocontainer.behaviors.Caching;
import com.picocontainer.behaviors.Guarding;
import com.picocontainer.behaviors.Storing;
import com.picocontainer.monitors.NullComponentMonitor;

import ognl.OgnlRuntime;
import com.picocontainer.web.PicoServletContainerListener;
import com.picocontainer.web.ScopedContainers;
import com.picocontainer.web.ThreadLocalLifecycleState;

import javax.servlet.ServletContextEvent;

@SuppressWarnings("serial")
public class Struts2PicoServletContainerListener extends PicoServletContainerListener {

    public void contextInitialized(ServletContextEvent event) {
        OgnlRuntime.setSecurityManager(null);
        super.contextInitialized(event);
    }

    @Override
    protected ScopedContainers makeScopedContainers(boolean stateless) {
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

    }

    /**
     * Struts2 handles whole value objects in some configurations.
     * This enables lazy instantiation of them    
     */
    @Override
    protected ComponentMonitor makeRequestComponentMonitor() {
        return new StrutsActionInstantiatingComponentMonitor();
    }

	public static class StrutsActionInstantiatingComponentMonitor extends NullComponentMonitor {
        public Object noComponentFound(MutablePicoContainer mutablePicoContainer, Object o) {
            return noComponent(mutablePicoContainer, o);
        }

        private Object noComponent(MutablePicoContainer mutablePicoContainer, Object o) {
            if (o instanceof Class) {
                Class<?> clazz = (Class<?>) o;
                if (Action.class.isAssignableFrom(clazz) || Result.class.isAssignableFrom(clazz)) {
                    try {
                        mutablePicoContainer.addComponent(clazz);
                    } catch (NoClassDefFoundError e) {
                        if (e.getMessage().equals("org/apache/velocity/context/Context")) {
                            // half expected. XWork seems to setup stuff that cannot
                            // work
                            // TODO if this is the case we should make configurable
                            // the list of classes we "expect" not to find.  Odd!
                        } else {
                            throw e;
                        }
                    }

                    return null;
                }
                try {
                    if (clazz.getConstructor(new Class[0]) != null) {
                        return clazz.newInstance();
                    }
                } catch (InstantiationException e) {
                    throw new PicoCompositionException("can't instantiate " + o);
                } catch (IllegalAccessException e) {
                    throw new PicoCompositionException("illegal access " + o);
                } catch (NoSuchMethodException e) {
                }
            }
            return super.noComponentFound(mutablePicoContainer, o);
        }
    }
}
