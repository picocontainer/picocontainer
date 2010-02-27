/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.jetty;

import org.mortbay.jetty.webapp.WebXmlConfiguration;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.FilterHolder;
import org.picocontainer.PicoContainer;
import org.picocontainer.DefaultPicoContainer;


public final class PicoWebXmlConfiguration extends WebXmlConfiguration {

    private final PicoContainer parentContainer;

    public PicoWebXmlConfiguration(PicoContainer parentContainer) {
        this.parentContainer = parentContainer;
    }

    protected ServletHolder newServletHolder() {
        return new PicoServletHolder(parentContainer);
    }

    protected FilterHolder newFilterHolder() {
        return new PicoFilterHolder(parentContainer);
    }

    protected Object newListenerInstance(Class clazz) throws InstantiationException, IllegalAccessException {
        DefaultPicoContainer child = new DefaultPicoContainer(parentContainer);
        child.addComponent("listener", clazz);
        return child.getComponent("listener");
    }

}
