/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.jetty;

import java.util.EventListener;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;

public class PicoContext {

    private final ServletContextHandler context;
    private final PicoContainer parentContainer;

    public static final int DEFAULT = 0;
    public static final int REQUEST = 1;
    public static final int FORWARD = 2;
    public static final int INCLUDE = 4;
    public static final int ERROR = 8;
    public static final int ALL = 15;

    public PicoContext(ServletContextHandler context, PicoContainer parentContainer, boolean withSessionHandler) {
        this.context = context;
        this.parentContainer = parentContainer;
    }

    public ServletHolder addServletWithMapping(Class<? extends Servlet> servletClass, String pathMapping) {
    	ServletHolder holder = new ServletHolder(servletClass);
        context.addServlet(holder, pathMapping);
        return holder;
    }

    public Servlet addServletWithMapping(Servlet servlet, String pathMapping) {
        ServletHolder holder = new ServletHolder(servlet);
        context.addServlet(holder, pathMapping);
        return servlet;
    }

    public FilterHolder addFilterWithMapping(Class<? extends Filter> filterClass, String pathMapping, int dispatchers) {
    	FilterHolder filterHolder = new FilterHolder(filterClass);
        context.addFilter(filterHolder, pathMapping, dispatchers);
        return filterHolder;
    }

    public FilterHolder addFilterWithMappings(Class<? extends Filter> filterClass, String[] pathMappings, int dispatchers) {
    	FilterHolder filterHolder = new FilterHolder(filterClass);
        for (String pathMapping : pathMappings) {
            context.addFilter(filterHolder, pathMapping, dispatchers);
        }
        return filterHolder;
    }

    public Filter addFilterWithMapping(Filter filter, String pathMapping, int dispatchers) {
        context.addFilter(new FilterHolder(filter), pathMapping, dispatchers);
        return filter;
    }

    public void addInitParam(String param, String value) {
    	context.setInitParameter(param, value);
    }


    public EventListener addListener(Class<?> listenerClass) {
        DefaultPicoContainer child = new DefaultPicoContainer(parentContainer);
        child.addComponent(EventListener.class, listenerClass);
        EventListener instance = child.getComponent(EventListener.class);
        return addListener(instance);
    }

    public EventListener addListener(EventListener listener) {
        context.addEventListener(listener);
        return listener;
    }


    public void setStaticContext(String absolutePath) {
        context.addServlet(DefaultServlet.class.getName(), "/");
        context.setResourceBase(absolutePath);
    }

    public void setStaticContext(String absolutePath, String welcomePage) {
        context.addServlet(DefaultServlet.class.getName(), "/");
        context.setResourceBase(absolutePath);
        context.setWelcomeFiles(new String[]{welcomePage});
    }

    public void setVirtualHosts(String... virtualhosts) {
        context.setVirtualHosts(virtualhosts);
    }

    public void addVirtualHost(String virtualhost) {
        String[] virtualHosts = context.getVirtualHosts();
        if (virtualHosts == null) {
            setVirtualHosts(virtualhost);
        } else {
            String[] newHosts = new String[virtualHosts.length +1];
            System.arraycopy(virtualHosts,0,newHosts,0,virtualHosts.length);
            newHosts[virtualHosts.length] = virtualhost;
            setVirtualHosts(newHosts);
        }
    }



    public void setDefaultHandling(final String absolutePath, String scratchDir, String pageSuffix) {
        context.setResourceBase(absolutePath);
        ServletHolder jspHolder = new ServletHolder();
        jspHolder.setName("jsp");
        jspHolder.setClassName("org.apache.jasper.servlet.JspServlet");
        jspHolder.setInitParameter("scratchdir", scratchDir);
        jspHolder.setInitParameter("logVerbosityLevel", "DEBUG");
        jspHolder.setInitParameter("fork", "false");
        jspHolder.setInitParameter("xpoweredBy", "false");
        jspHolder.setForcedPath(null);
        jspHolder.setInitOrder(0);

        context.addServlet(jspHolder, "*.jsp");
        context.addServlet(DefaultServlet.class.getName(), "/");

    }

    public void addErrorHandler() {
        addErrorHandler(new ErrorPageErrorHandler());
    }

    public void addErrorHandler(ErrorHandler handler) {
        context.setErrorHandler(handler);
    }

}
