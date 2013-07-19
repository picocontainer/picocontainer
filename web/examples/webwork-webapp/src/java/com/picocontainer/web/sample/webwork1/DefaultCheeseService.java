/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web.sample.webwork1;

import java.io.Serializable;
import java.util.Collection;

/**
 * Default implementation of CheeseService. Typically it will be used a proxy to
 * delegate execution of the MVC-framework specific action.
 *
 * @author Stephen Molitor
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class DefaultCheeseService implements CheeseService, Serializable {

    private final CheeseDao dao;

    public DefaultCheeseService(final CheeseDao dao) {
        this.dao = dao;
    }

    public Collection<Cheese> getCheeses() {
        return dao.all();
    }

    public Cheese find(final Cheese example) {
        return dao.get(example.getName());
    }

    public void save(final Cheese cheese) {
        dao.save(cheese);
    }

    public void remove(final Cheese cheese) {
        dao.remove(cheese);
    }
}