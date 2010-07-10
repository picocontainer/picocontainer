/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web;

import java.io.IOException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Characteristics;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.lifecycle.DefaultLifecycleState;
import org.picocontainer.adapters.AbstractAdapter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

@SuppressWarnings("serial")
public abstract class PicoServletContainerFilter implements Filter, Serializable {

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
    }

    private ScopedContainers getScopedContainers(ServletContext context) {
        return (ScopedContainers) context.getAttribute(ScopedContainers.class.getName());
    }

    protected void initAdditionalScopedComponents(MutablePicoContainer sessionContainer, MutablePicoContainer reqContainer) {
    }

    public static Object getRequestComponentForThread(Class<?> type) {
        MutablePicoContainer requestContainer = ServletFilter.currentRequestContainer.get();
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
        setRequestContainer(null);

        scopedContainers.getRequestContainer().stop();
        scopedContainers.getRequestContainer().dispose();

        if (!isStateless) {
            if (printSessionSize) {
                printSessionSizeDetailsForDebugging(ssh);
            }
            sess.setAttribute(SessionStoreHolder.class.getName(), ssh);
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

    private void printSessionSizeDetailsForDebugging(SessionStoreHolder ssh) throws IOException {
        if (debug) {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream(baos);
	        oos.writeObject(ssh);
	        oos.close();
	        baos.close();
	        String xml = new XStream(new PureJavaReflectionProvider()).toXML(ssh);
	        int bytes = baos.toByteArray().length;        
	        System.out.println("** Session written (" + bytes + " bytes), xml representation= " + xml);
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

    public static class ServletFilter extends PicoServletContainerFilter {

        private static ThreadLocal<MutablePicoContainer> currentAppContainer = new ThreadLocal<MutablePicoContainer>();
        private static ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
        private static ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();

        protected void setAppContainer(MutablePicoContainer container) {
            if (currentRequestContainer == null) {
                currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
            }
            currentAppContainer.set(container);
        }

        protected void setRequestContainer(MutablePicoContainer container) {
            if (currentRequestContainer == null) {
                currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
            }
            currentRequestContainer.set(container);
        }

        protected void setSessionContainer(MutablePicoContainer container) {
            if (currentSessionContainer == null) {
                currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
            }
            currentSessionContainer.set(container);
        }
    }

    public static class HttpSessionInjector extends AbstractAdapter {

        public HttpSessionInjector() {
            super(HttpSession.class, HttpSession.class);
        }

        public Object getComponentInstance(PicoContainer picoContainer, Type type) throws PicoCompositionException {
            return currentSession.get();
        }

        public void verify(PicoContainer picoContainer) throws PicoCompositionException {
        }

        public String getDescriptor() {
            return "HttpSessionInjector";
        }
    }

    public static class HttpServletRequestInjector extends AbstractAdapter {

        public HttpServletRequestInjector() {
            super(HttpServletRequest.class, HttpServletRequest.class);
        }

        public Object getComponentInstance(PicoContainer picoContainer, Type type) throws PicoCompositionException {
            return currentRequest.get();
        }

        public void verify(PicoContainer picoContainer) throws PicoCompositionException {
        }

        public String getDescriptor() {
            return "HttpServletRequestInjector";
        }
    }

    public static class HttpServletResponseInjector extends AbstractAdapter {

        public HttpServletResponseInjector() {
            super(HttpServletResponse.class, HttpServletResponse.class);
        }

        public Object getComponentInstance(PicoContainer picoContainer, Type type) throws PicoCompositionException {
            return currentResponse.get();
        }

        public void verify(PicoContainer picoContainer) throws PicoCompositionException {
        }

        public String getDescriptor() {
            return "HttpServletResponseInjector";
        }
    }
}
