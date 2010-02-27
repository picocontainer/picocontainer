/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.jetty;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.BlockingChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.picocontainer.PicoContainer;
import org.picocontainer.Startable;
import org.picocontainer.containers.EmptyPicoContainer;

public class PicoJettyServer extends EmptyPicoContainer implements PicoContainer, Startable {

    private final Server server;
    private final PicoContainer parentContainer;
    private ErrorHandler errorHandler;

    public PicoJettyServer(PicoContainer parentContainer) {
        this.parentContainer = parentContainer;
        server = new Server();
        server.setHandler(new HandlerList());
    }

    public PicoJettyServer(String host, int port, PicoContainer parentContainer) {
        this(parentContainer);
        createBlockingChannelConnector(host, port);
    }
    public PicoJettyServer(String host, int port, PicoContainer parentContainer, int timeout) {
        this(parentContainer);
        createBlockingChannelConnector(host, port, timeout);
    }

    public Connector createBlockingChannelConnector(String host, int port) {
        return createBlockingChannelConnector(host, port, 10*1000);
    }

    public Connector createBlockingChannelConnector(String host, int port, int timeout) {
        BlockingChannelConnector connector = new BlockingChannelConnector();
        connector.setHost(host);
        connector.setPort(port);
        connector.setLowResourceMaxIdleTime(timeout);
        server.addConnector(connector);
        return connector;
    }

    public PicoContext createContext(String contextPath, boolean withSessionHandler) {
        Context context = new Context(server, contextPath, Context.SESSIONS);
        return new PicoContext(context, parentContainer, withSessionHandler);
    }


    public PicoWebAppContext addWebApplication(String contextPath, String warFile) {
        PicoWebAppContext wah = new PicoWebAppContext(parentContainer);
        wah.setContextPath(contextPath);
        wah.setExtractWAR(true);
        wah.setWar(warFile);
        wah.setParentLoaderPriority(true);
        server.addHandler(wah);
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
        server.addHandler(requestLogHandler);

    }

}
