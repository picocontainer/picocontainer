/*******************************************************************************
 * Copyright (c) PicoContainer Organization. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web.remoting;

import java.io.IOException;
import java.lang.reflect.Member;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.picocontainer.ComponentAdapter;
import com.picocontainer.ComponentMonitor;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.PicoContainer;
import com.picocontainer.monitors.NullComponentMonitor;
import com.picocontainer.web.PicoServletFilter;
import com.thoughtworks.xstream.XStream;

/**
 * Abstract Servlet used for the calling of methods in a tree of components managed by PicoContainer.
 * The form of the reply is determined by the XStream implementation passed into the constructor,
 * the request is plainly mapped from Query Strings and form fields to the method signature.
 *
 * @author Paul Hammant
 */
@SuppressWarnings("serial")
public abstract class AbstractPicoWebRemotingServlet extends HttpServlet {
	
	private PicoHook picoHook = new PicoHook();

    private static final String EMPTY = "";
	private static final String DOT = ".";
	/*
	private static final String COLON = ":";
	private static final String SPACE = " ";
	private static final String GET = "GET";
	private static final String CLOSE = "].";
	private static final String OPEN = "[";
	*/
	private static final String SLASH = "/";
    private static final String APPLICATION_SCOPE = "application";
    private static final String SESSION_SCOPE = "session";
    private static final String REQUEST_SCOPE = "request";
    private static final String SCOPES_TO_PUBLISH = "scopes_to_publish";
    private static final String PACKAGE_PREFIX_TO_STRIP = "package_prefix_to_strip";
    private static final String SUFFIX_TO_STRIP = "suffix_to_strip";
    private static final String MIME_TYPE = "mime_type";
    private static final String LOWER_CASE_PATH = "lower_case_path";
    private static final String USE_METHOD_NAME_PREFIXES_FOR_VERBS = "use_method_name_prefixes_for_verbs";



	private XStream xstream;
    private PicoWebRemoting pwr;
    private String mimeType = "text/plain";
    private PicoWebRemotingMonitor monitor;

    private volatile boolean initialized;

    protected abstract XStream createXStream();
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	if (!initialized) {
    		initialize();
    	}
        respond(request, response, request.getPathInfo());
    }
    
    /**
     * Synchronized with double-lock checking before calling.
     */
    private synchronized void initialize() {
        if (!initialized) {
            publishAdapters();
            initialized = true;
        }
    }

    protected void respond(final HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
        response.setContentType(mimeType);

        final String httpMethod = request.getMethod();

        //final Cache cache = currentAppContainer.get().getComponent(Cache.class);

        String result = pwr.processRequest(pathInfo, picoHook.getCurrentRequestContainer(), httpMethod, new NullComponentMonitor() {
                            @Override
                            public Object invoking(PicoContainer container, ComponentAdapter<?> componentAdapter, Member member, Object instance, Object... args) {
                                return ComponentMonitor.KEEP;
                            }

                            @Override
                            public void invoked(PicoContainer container, ComponentAdapter<?> componentAdapter, Member member, Object instance, long duration, Object retVal, Object... args) {
                                // Empty
                            }
                        });

        if (result != null) {
            response.getWriter().print(result);
        } else {
            response.sendError(400, "Nothing is mapped to this URL, try removing the last term for directory list.");
        }
    }


    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
    	this.xstream = createXStream();
        String packagePrefixToStrip = servletConfig.getInitParameter(PACKAGE_PREFIX_TO_STRIP);
        String prefixToStripFromUrls;
        if (packagePrefixToStrip == null) {
            prefixToStripFromUrls = EMPTY;
        } else {
            prefixToStripFromUrls = packagePrefixToStrip.replace(DOT, SLASH) + SLASH;
        }

        String suffixToStrip = servletConfig.getInitParameter(SUFFIX_TO_STRIP);

        String scopesToPublish = servletConfig.getInitParameter(SCOPES_TO_PUBLISH);
        if (scopesToPublish == null) {
            scopesToPublish = EMPTY;
        }
        String mimeTypeFromConfig = servletConfig.getInitParameter(MIME_TYPE);
        if (mimeTypeFromConfig != null) {
            mimeType = mimeTypeFromConfig;
        }

        String lowerCasePathStr = servletConfig.getInitParameter(LOWER_CASE_PATH);
        boolean lowerCasePath;
        if (lowerCasePathStr == null) {
            lowerCasePath = false;
        } else {
            lowerCasePath = lowerCasePathStr.toLowerCase().equals(Boolean.TRUE.toString());
        }

        String useMethodNamePrefixesForVerbsStr = servletConfig.getInitParameter(USE_METHOD_NAME_PREFIXES_FOR_VERBS);
        boolean useMethodNamePrefixesForVerbs;
        if (useMethodNamePrefixesForVerbsStr == null) {
            useMethodNamePrefixesForVerbs = true;
        } else {
            useMethodNamePrefixesForVerbs = lowerCasePathStr.toLowerCase().equals(Boolean.TRUE.toString());
        }

        super.init(servletConfig);
        pwr = new PicoWebRemoting(xstream, prefixToStripFromUrls, suffixToStrip, scopesToPublish, lowerCasePath, useMethodNamePrefixesForVerbs);
    }

    private void publishAdapters() {
        pwr.publishAdapters(picoHook.getCurrentRequestContainer().getComponentAdapters(), REQUEST_SCOPE);
        MutablePicoContainer sessionContainer = picoHook.getCurrentSessionContainer();
        if (sessionContainer != null) {
            pwr.publishAdapters(sessionContainer.getComponentAdapters(), SESSION_SCOPE);
        }
        pwr.publishAdapters(picoHook.getAppContainer().getComponentAdapters(), APPLICATION_SCOPE);

        monitor = picoHook.getAppContainer().getComponent(PicoWebRemotingMonitor.class);
        if (monitor == null) {
            monitor = new NullPicoWebRemotingMonitor();
        }
        pwr.setMonitor(monitor);
    }

    protected void visitClass(String clazz, MethodVisitor mapv) throws IOException {
        pwr.visitClass(clazz, picoHook.getCurrentRequestContainer(), mapv);
    }

    private static class PicoHook extends PicoServletFilter {
    	
    	MutablePicoContainer getAppContainer() {
    		return super.getApplicationContainer();
    	}
    	
    	MutablePicoContainer getCurrentSessionContainer() {
    		return super.getSessionContainer();
    	}
    	
    	MutablePicoContainer getCurrentRequestContainer() {
    		return super.getRequestContainer();
    	}
    	
    }
}
