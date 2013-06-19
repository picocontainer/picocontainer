/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web;

import org.picocontainer.*;
import org.picocontainer.adapters.AbstractAdapter;
import org.picocontainer.lifecycle.DefaultLifecycleState;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;

@SuppressWarnings("serial")
public abstract class AbstractPicoServletContainerFilter implements Filter, Serializable {

    private boolean exposeServletInfrastructure;
    private boolean isStateless;
    private boolean printSessionSize;
	private boolean debug = false;

    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext context = filterConfig.getServletContext();
        ScopedContainers scopedContainers = getScopedContainers(context);
        setAppContainer(scopedContainers.getApplicationContainer());

        isStateless = Boolean.parseBoolean(context.getInitParameter(PicoServletContainerListener.STATELESS_WEBAPP));
        printSessionSize = Boolean.parseBoolean(context.getInitParameter(PicoServletContainerListener.PRINT_SESSION_SIZE));

        String exposeServletInfrastructureString = filterConfig.getInitParameter("exposeServletInfrastructure");
        if (exposeServletInfrastructureString == null || Boolean.parseBoolean(exposeServletInfrastructureString)) {
            exposeServletInfrastructure = true;
        }

        scopedContainers.getRequestContainer().as(Characteristics.NO_CACHE).addAdapter(new HttpSessionInjector());
        scopedContainers.getRequestContainer().as(Characteristics.NO_CACHE).addAdapter(new HttpServletRequestInjector());
        scopedContainers.getRequestContainer().as(Characteristics.NO_CACHE).addAdapter(new HttpServletResponseInjector());

        initAdditionalScopedComponents(scopedContainers.getSessionContainer(), scopedContainers.getRequestContainer());

    }

    public void destroy() {
    	if (PicoServletFilter.currentRequestContainer != null)  PicoServletFilter.currentRequestContainer.remove(); 
    	if (PicoServletFilter.currentSessionContainer != null)  PicoServletFilter.currentSessionContainer.remove(); 
    	if (PicoServletFilter.currentAppContainer != null)  PicoServletFilter.currentAppContainer.remove(); 
    }

    private ScopedContainers getScopedContainers(ServletContext context) {
        return (ScopedContainers) context.getAttribute(ScopedContainers.class.getName());
    }

    protected void initAdditionalScopedComponents(MutablePicoContainer sessionContainer, MutablePicoContainer reqContainer) {
    }

    public static Object getRequestComponentForThread(Class<?> type) {
        MutablePicoContainer requestContainer = PicoServletFilter.currentRequestContainer.get();
        MutablePicoContainer container = new DefaultPicoContainer(requestContainer);
        container.addComponent(type);
        return container.getComponent(type);
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) req;
        HttpSession sess = servletRequest.getSession();
        if (exposeServletInfrastructure) {
            currentSession.set(sess);
            currentRequest.set(req);
            currentResponse.set(resp);
        }

        ScopedContainers scopedContainers = getScopedContainers(sess.getServletContext());

        SessionStoreHolder ssh = null;
        if (!isStateless) {

            ssh = (SessionStoreHolder) sess.getAttribute(SessionStoreHolder.class.getName());
            if (ssh == null) {
                if (scopedContainers.getSessionContainer().getComponentAdapters().size() > 0) {
                    throw new PicoContainerWebException("Session not setup correctly.  There are components registered " +
                            "at the session level, but no working container to host them");
                }
                ssh = new SessionStoreHolder(scopedContainers.getSessionStoring().getCacheForThread(), new DefaultLifecycleState());
            }

            scopedContainers.getSessionStoring().putCacheForThread(ssh.getStoreWrapper());
            scopedContainers.getSessionState().putLifecycleStateModelForThread(ssh.getLifecycleState());

        }
        scopedContainers.getRequestStoring().resetCacheForThread();
        scopedContainers.getRequestState().resetStateModelForThread();

        scopedContainers.getRequestContainer().start();

        setAppContainer(scopedContainers.getApplicationContainer());
        if (!isStateless) {
            setSessionContainer(scopedContainers.getSessionContainer());
        }
        setRequestContainer(scopedContainers.getRequestContainer());
        
        containersSetupForRequest(scopedContainers.getApplicationContainer(), scopedContainers.getSessionContainer(), scopedContainers.getRequestContainer(), req, resp);

        filterChain.doFilter(req, resp);

        setAppContainer(null);
        if (!isStateless) {
            setSessionContainer(null);
        }

        scopedContainers.getRequestContainer().stop();
        scopedContainers.getRequestContainer().dispose();
        setRequestContainer(null);

        if (!isStateless) {
            if (printSessionSize) {
                PrintSessionSizeDetailsForDebugging.printItIfDebug(debug, ssh);
            }
            try {
                sess.setAttribute(SessionStoreHolder.class.getName(), ssh);
            }
            catch (IllegalStateException ex) {
                // catalina can report 'Session already invalidated'
            }
        }
        scopedContainers.getRequestStoring().invalidateCacheForThread();
        scopedContainers.getRequestState().invalidateStateModelForThread();

        if (!isStateless) {
            scopedContainers.getSessionStoring().invalidateCacheForThread();
            scopedContainers.getSessionState().invalidateStateModelForThread();
        }

        if (exposeServletInfrastructure) {
            currentSession.set(null);
            currentRequest.set(null);
            currentResponse.set(null);
        }

    }

    protected void containersSetupForRequest(MutablePicoContainer appcontainer, MutablePicoContainer sessionContainer,
                                             MutablePicoContainer requestContainer, ServletRequest req, ServletResponse resp) {
    }

    private static ThreadLocal<HttpSession> currentSession = new ThreadLocal<HttpSession>();
    private static ThreadLocal<ServletRequest> currentRequest = new ThreadLocal<ServletRequest>();
    private static ThreadLocal<ServletResponse> currentResponse = new ThreadLocal<ServletResponse>();

    protected abstract void setAppContainer(MutablePicoContainer container);

    protected abstract void setSessionContainer(MutablePicoContainer container);

    protected abstract void setRequestContainer(MutablePicoContainer container);

    public static class HttpSessionInjector extends AbstractAdapter<HttpSession> {

        public HttpSessionInjector() {
            super(HttpSession.class, HttpSession.class);
        }

        public HttpSession getComponentInstance(PicoContainer picoContainer, Type type) throws PicoCompositionException {
            return currentSession.get();
        }

        public void verify(PicoContainer picoContainer) throws PicoCompositionException {
        }

        public String getDescriptor() {
            return "HttpSessionInjector";
        }
    }

    public static class HttpServletRequestInjector extends AbstractAdapter<HttpServletRequest> {

        public HttpServletRequestInjector() {
            super(HttpServletRequest.class, HttpServletRequest.class);
        }

        public HttpServletRequest getComponentInstance(PicoContainer picoContainer, Type type) throws PicoCompositionException {
            return (HttpServletRequest) currentRequest.get();
        }

        public void verify(PicoContainer picoContainer) throws PicoCompositionException {
        }

        public String getDescriptor() {
            return "HttpServletRequestInjector";
        }
    }

    public static class HttpServletResponseInjector extends AbstractAdapter<HttpServletResponse> {

        public HttpServletResponseInjector() {
            super(HttpServletResponse.class, HttpServletResponse.class);
        }

        public HttpServletResponse getComponentInstance(PicoContainer picoContainer, Type type) throws PicoCompositionException {
            return (HttpServletResponse) currentResponse.get();
        }

        public void verify(PicoContainer picoContainer) throws PicoCompositionException {
        }

        public String getDescriptor() {
            return "HttpServletResponseInjector";
        }
    }
}
