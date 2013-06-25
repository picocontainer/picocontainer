/**
 * **************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 * *
 * ***************************************************************************
 */
package com.picocontainer.web.sample.struts;

import java.util.Collection;

/**
 * @author Stephen Molitor
 */
public interface CheeseDao {

    void save(Cheese cheese);

    void remove(Cheese cheese);

    Cheese get(String name);

    Collection all();

}