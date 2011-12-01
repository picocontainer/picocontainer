/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.web.sample.struts2;

import java.util.Collection;

/**
 * This is a service which is independent of any MVC framework.
 *
 * @author Mauro Talevi
 */
public interface CheeseService {

    public Collection getCheeses();

    public Cheese find(Cheese example);

    public void save(Cheese cheese);

    public void remove(Cheese cheese);

}