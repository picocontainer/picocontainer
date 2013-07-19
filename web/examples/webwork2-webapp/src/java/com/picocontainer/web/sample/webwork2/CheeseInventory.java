/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web.sample.webwork2;

import com.opensymphony.xwork.Action;

import java.util.List;
import java.util.ArrayList;

/**
 * Example of a XWork action that relies on constructor injection.
 * 
 * @author Paul Hammant
 */
public class CheeseInventory implements Action {

    private final CheeseService cheeseService;
    private List<Cheese> cheeses;

    public CheeseInventory(CheeseService cheeseService) {
        this.cheeseService = cheeseService;
    }

    public List<Cheese> getCheeses() {
        return cheeses;
    }

    public String execute() throws Exception {
        cheeses = new ArrayList<Cheese>(cheeseService.getCheeses());
        return SUCCESS;
    }

}


