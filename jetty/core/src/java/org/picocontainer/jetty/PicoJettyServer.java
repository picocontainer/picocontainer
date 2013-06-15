/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.containers.EmptyPicoContainer;

@SuppressWarnings("serial")
public class PicoJettyServer extends EmptyPicoContainer implements PicoContainer, Startable {

    private final Server server;
    private final PicoContainer parentContainer;
    
    private final HandlerList handlerList;

    public PicoJettyServer(PicoContainer parentContainer) {
        this.parentContainer = parentContainer;
        server = new Server();
        //server.setHandler(new HandlerList());
        handlerList = new HandlerList();
        server.setHandler(handlerList);
    }

    public PicoJettyServer(String host, int port, PicoContainer parentContainer) {
        this(parentContainer);
        createServerConnector(host, port);
    }
    public PicoJettyServer(String host, int port, PicoContainer parentContainer, int timeout) {
        this(parentContainer);
        createServerConnector(host, port, timeout);
    }


    /**
     * Use {@link #createBlockingChannelConnector(String, int)} instead.
     * @param host
     * @param port
     * @return
     * @deprecated
     */
    @Deprecated
    public Connector createBlockingChannelConnector(String host, int port) {
    	return createServerConnector(host, port);
    }
    
    
    public Connector createServerConnector(String host, int port) {
        return createServerConnector(host, port, 10*1000);
    }
    
    /**
     * Use {@link #createServerConnector(String, int, int)} instead.
     * @param host
     * @param port
     * @param timeout
     * @return
     * @deprecated
     */
    @Deprecated
    public Connector createBlockingChannelConnector(String host, int port, int timeout) {
    	return this.createServerConnector(host, port, timeout);
    }

    public Connector createServerConnector(String host, int port, int timeout) {
    	ServerConnector connector = new ServerConnector(server);
        connector.setHost(host);
        connector.setPort(port);
        connector.setIdleTimeout(timeout);
        server.addConnector(connector);
        return connector;
    }

    public PicoContext createContext(String contextPath, boolean withSessionHandler) {
        ServletContextHandler context = new PicoServletContextHandler(parentContainer, server, contextPath, ServletContextHandler.SESSIONS);
        PicoContext picoContext =  new PicoContext(context, parentContainer, withSessionHandler);
        //?
        handlerList.addHandler(context);
        return picoContext;
    }


    public PicoWebAppContext addWebApplication(String contextPath, String warFile) {
        PicoWebAppContext wah = new PicoWebAppContext(parentContainer);
        wah.setContextPath(contextPath);
        wah.setExtractWAR(true);
        wah.setWar(warFile);
        wah.setParentLoaderPriority(true);

        handlerList.addHandler(wah);
        return wah;
    }


    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new JettyServerLifecycleException("Jetty couldn't start", e);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new JettyServerLifecycleException("Jetty couldn't stop", e);
        }
    }

    public void addRequestLog(RequestLog requestLog) {
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);
        handlerList.addHandler(requestLogHandler);

    }

}
