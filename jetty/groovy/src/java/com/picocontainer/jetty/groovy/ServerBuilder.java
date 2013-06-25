/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package com.picocontainer.jetty.groovy;

import groovy.util.NodeBuilder;

import java.util.Map;

import com.picocontainer.jetty.PicoContext;
import com.picocontainer.jetty.PicoJettyServer;
import com.picocontainer.jetty.PicoWebAppContext;

import com.picocontainer.MutablePicoContainer;

public class ServerBuilder extends NodeBuilder {
    private final PicoJettyServer server;
    private final MutablePicoContainer parentContainer;

    public ServerBuilder(final PicoJettyServer server, final MutablePicoContainer parentContainer) {
        this.server = server;
        this.parentContainer = parentContainer;
    }

    @Override
	protected Object createNode(final Object name, final Map map) {
        if (name.equals("context")) {
            return createContext(map);
        } else if (name.equals("blockingChannelConnector")) {
            return createBlockingChannelConnector(map);
        } else if (name.equals("xmlWebApplication")) {
            return createXmlWebApplication(map);
        }
        return null;
    }

    protected Object createBlockingChannelConnector(final Map map) {
        int port = (Integer)map.remove("port");
        return server.createServerConnector((String) map.remove("host"), port);
    }

    protected Object createContext(final Map map) {
        boolean sessions = false;
        if (map.containsKey("sessions")) {
            sessions = Boolean.valueOf((String)map.remove("sessions"));
        }
        PicoContext context = server.createContext((String) map.remove("path"), sessions);
        return new ContextBuilder(parentContainer, context);
    }

    protected Object createXmlWebApplication(final Map map) {
        PicoWebAppContext context = server.addWebApplication((String) map.remove("path"), (String) map.remove("warfile"));
        return new WarFileBuilder(parentContainer, context);
    }

}
