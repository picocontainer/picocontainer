/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/

package org.picocontainer.jetty;

import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.picocontainer.PicoContainer;

public class PicoServletHandler extends ServletHandler {

    private final PicoContainer parentContainer;

    public PicoServletHandler(PicoContainer parentContainer) {
        this.parentContainer = parentContainer;
    }

    public ServletHolder newServletHolder(Class servletClass) {
        return new PicoServletHolder(servletClass, parentContainer);
    }

    public ServletHolder newServletHolder() {
        return new PicoServletHolder(parentContainer);
    }

    public FilterHolder newFilterHolder(Class filterClass) {
        return new PicoFilterHolder(filterClass, parentContainer);
    }

    public FilterHolder newFilterHolder() {
        return new PicoFilterHolder(parentContainer);
    }
}