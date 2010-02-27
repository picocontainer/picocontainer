/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.jetty;

import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.jetty.webapp.WebXmlConfiguration;
import org.picocontainer.PicoContainer;

public class PicoWebAppContext extends WebAppContext {
    private final PicoContainer parentContainer;

    public PicoWebAppContext(PicoContainer parentContainer) {
             super(null,null,new PicoServletHandler(parentContainer),null);
        this.parentContainer = parentContainer;
    }

    boolean doSuperIsRunning = true;

    protected void loadConfigurations() throws Exception {
        super.loadConfigurations();
        Configuration[]  configurations = getConfigurations();
        for (int i = 0; i < configurations.length; i++) {
            if (configurations[i] instanceof WebXmlConfiguration) {
                configurations[i] = new PicoWebXmlConfiguration(parentContainer);
            }
        }
        doSuperIsRunning = false;
        setConfigurations(configurations);
        doSuperIsRunning = true; 
    }

    @Override
    public boolean isRunning() {
        if (doSuperIsRunning) {
            return super.isRunning();
        } else {
            return false;
        }
    }
    /* ------------------------------------------------------------ */
    public ServletHandler getServletHandler() {
        return super.getServletHandler();
    }
}
