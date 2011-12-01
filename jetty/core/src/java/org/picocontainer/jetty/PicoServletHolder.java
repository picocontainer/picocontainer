/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.jetty;

import org.mortbay.jetty.servlet.ServletHolder;
import org.picocontainer.PicoContainer;
import org.picocontainer.DefaultPicoContainer;

import javax.servlet.Servlet;

public class PicoServletHolder extends ServletHolder {

    private final PicoContainer parentContainer;

    public PicoServletHolder(PicoContainer parentContainer) {
        this.parentContainer = parentContainer;
    }


    public PicoServletHolder(Class clazz, PicoContainer parentContainer) {
        super(clazz);
        this.parentContainer = parentContainer;
    }

    public synchronized Object newInstance() throws InstantiationException, IllegalAccessException {
        DefaultPicoContainer child = new DefaultPicoContainer(parentContainer);
        child.addComponent(Servlet.class, _class);
        return child.getComponent(Servlet.class);
    }

}