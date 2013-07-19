/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package com.picocontainer.web.sample.webwork1;

import javax.servlet.ServletContext;

import com.picocontainer.Characteristics;
import com.picocontainer.MutablePicoContainer;
import com.picocontainer.web.WebappComposer;

public class WebWork1DemoComposer implements WebappComposer {

    public void composeApplication(final MutablePicoContainer container, final ServletContext context) {
        container.addComponent(CheeseDao.class, InMemoryCheeseDao.class);
    }

    public void composeSession(final MutablePicoContainer container) {
        container.addComponent(CheeseService.class, DefaultCheeseService.class);
    }

    public void composeRequest(final MutablePicoContainer container) {
        container.as(Characteristics.NO_CACHE).addComponent(Brand.class, Brand.FromRequest.class);

    }

}