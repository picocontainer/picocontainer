/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.webwork.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;
import org.picocontainer.PicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.containers.EmptyPicoContainer;
import org.picocontainer.web.PicoServletContainerFilter;

import webwork.action.ServletActionContext;
import webwork.util.ServletValueStack;
import webwork.view.velocity.WebWorkUtil;

/**
 * velocity integration servlet. integrates container hieararchy into velocity
 * context as well webwork specific objects. This servlet is not derived from
 * standart webwork velocity servlet because it inherits from obsolete velocity
 * servlet (which does not allow resource loading from webapp ). acc
 * configuration is done like original velocity servlet does
 * 
 * @author Konstantin Pribluda
 */
@SuppressWarnings("serial")
public final class WebWorkVelocityServlet extends VelocityViewServlet {

    public static class ServletFilter extends PicoServletContainerFilter {
        private static ThreadLocal<MutablePicoContainer> currentRequestContainer = new ThreadLocal<MutablePicoContainer>();
        private static ThreadLocal<MutablePicoContainer> currentSessionContainer = new ThreadLocal<MutablePicoContainer>();
        private static ThreadLocal<MutablePicoContainer> currentAppContainer = new ThreadLocal<MutablePicoContainer>();

        protected void setAppContainer(MutablePicoContainer container) {
            currentAppContainer.set(container);
        }
        protected void setRequestContainer(MutablePicoContainer container) {
            currentRequestContainer.set(container);
        }
        protected void setSessionContainer(MutablePicoContainer container) {
            currentSessionContainer.set(container);
        }

        protected static MutablePicoContainer getRequestContainerForThread() {
            return currentRequestContainer.get();
        }
        protected static MutablePicoContainer getSessionContainerForThread() {
            return currentSessionContainer.get();
        }
        protected static MutablePicoContainer getApplicationContainerForThread() {
            return currentAppContainer.get();
        }
    }

    static final String WEBWORK_UTIL = "webwork";
    // those have to be removed once dependency problem is solved.
    // will bomb anyway.
    static final String REQUEST = "req";
    static final String RESPONSE = "res";

    static final EmptyPicoContainer emptyContainer = new EmptyPicoContainer();

    protected Context createContext(javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response) {
        Context ctx = new NanocontainerVelocityContext(ServletFilter.getRequestContainerForThread(),
                ServletValueStack.getStack(request));
        ctx.put(REQUEST, request);
        ctx.put(RESPONSE, response);
        return ctx;
    }

    /**
     * Get the template to show.
     */
    protected Template handleRequest(javax.servlet.http.HttpServletRequest aRequest,
            javax.servlet.http.HttpServletResponse aResponse, Context ctx) throws java.lang.Exception {
        // Bind standard WebWork utility into context

        ServletActionContext.setContext(aRequest, aResponse, getServletContext(), null);
        ctx.put(WEBWORK_UTIL, new WebWorkUtil(ctx));

        String servletPath = (String) aRequest.getAttribute("javax.servlet.include.servlet_path");
        if (servletPath == null)
            servletPath = aRequest.getServletPath();
        return getTemplate(servletPath);
    }

    static final class NanocontainerVelocityContext extends VelocityContext {
        final PicoContainer container;
        final ServletValueStack stack;

        NanocontainerVelocityContext(PicoContainer container, ServletValueStack stack) {
            this.container = container != null ? container : emptyContainer;
            this.stack = stack;
        }

        public boolean internalContainsKey(java.lang.Object key) {
            boolean contains = super.internalContainsKey(key);
            if (contains)
                return contains;

            contains = stack.test(key.toString());
            if (contains)
                return contains;

            return container.getComponentAdapter(key) != null;
        }

        public Object internalGet(String key) {
            if (super.internalContainsKey(key))
                return super.internalGet(key);

            if (stack.test(key))
                return stack.findValue(key);

            return container.getComponent(key);
        }
    }
}
