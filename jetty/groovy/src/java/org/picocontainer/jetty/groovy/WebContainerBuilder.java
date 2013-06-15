/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.jetty.groovy;

import java.util.Map;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.jetty.PicoJettyServer;
import org.picocontainer.script.groovy.nodes.AbstractBuilderNode;

public class WebContainerBuilder extends AbstractBuilderNode {


    public WebContainerBuilder() {
        super("webContainer");
    }

    public Object createNewNode(final Object current, final Map map) {
        int port = 0;
        if (map.containsKey("port")) {
            port = (Integer)map.remove("port");
        }
        String host;
        if (map.containsKey("host")) {
            host = (String) map.remove("host");
        } else {
            host = null;
        }
        int timeout = 10*1000;
        if (map.containsKey("timeout")) {
            timeout = (Integer) map.remove("timeout");
        }

        MutablePicoContainer parentContainer = (MutablePicoContainer) current;

        PicoJettyServer server;
        if (port != 0) {
            server = new PicoJettyServer(host, port, parentContainer, timeout);
        } else {
            server = new PicoJettyServer(parentContainer);
        }
        parentContainer.addChildContainer(server);
        return new ServerBuilder(server, parentContainer);
    }


}


