/*******************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.
 * ---------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.sample.struts2.pwr;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Characteristics;
import org.picocontainer.web.struts2.Struts2Composer;

import javax.servlet.ServletContext;

public class Struts2PwrComposer extends Struts2Composer {

    public void composeApplication(MutablePicoContainer container, ServletContext context) {
        super.composeApplication(container, context);
        container.addComponent(CheeseDao.class, InMemoryCheeseDao.class);
    }

    public void composeSession(MutablePicoContainer container) {
        container.addComponent(CheeseService.class, DefaultCheeseService.class);
    }

    public void composeRequest(MutablePicoContainer container) {
        container.as(Characteristics.NO_CACHE).addComponent(Brand.class, Brand.FromRequest.class);

    }

}