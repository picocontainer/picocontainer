/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/

package org.picocontainer.script.testmodel;

import junit.framework.Assert;
import org.picocontainer.Startable;

public class WebServerImpl implements WebServer, Startable {

    public WebServerImpl(WebServerConfig wsc) {
        this(wsc, new StringBuffer("d"));
    }

    public WebServerImpl(WebServerConfig wsc, StringBuffer sb) {
        Assert.assertTrue("No port number specified", wsc.getPort() > 0);
        Assert.assertNotNull("No host name specified", wsc.getHost());
        sb.append("-WebServerImpl:").append(wsc.getHost()).append(":").append(wsc.getPort());
    }

    public void start() {
    }

    public void stop() {
    }
}
