/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web.sample.webwork1;

import java.util.Collection;

import webwork.action.ActionSupport;
import webwork.action.CommandDriven;

/**
 * Example of a WebWork1 action that relies on constructor injection.
 *
 * @author Aslak Helles&oslash;y
 */
@SuppressWarnings("serial")
public final class CheeseAction extends ActionSupport implements CommandDriven {

    private final CheeseService cheeseService;
    private final Cheese cheese = new Cheese();

    public CheeseAction(final CheeseService cheeseService) {
        this.cheeseService = cheeseService;
    }

    public Cheese getCheese() {
        return cheese;
    }

    public Collection<Cheese> getCheeses() {
        return cheeseService.getCheeses();
    }

    public String doSave() {
        try {
            cheeseService.save(cheese);
            return SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Couldn't save cheese: " + cheese);
            return ERROR;
        }
    }

    public String doFind() {
        try {
            cheeseService.find(cheese);
            return SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Couldn't find cheese: "+ cheese);
            return ERROR;
        }
    }


    public String doRemove() {
        try {
        cheeseService.remove(cheeseService.find(cheese));
        return SUCCESS;
        } catch(Exception e) {
            e.printStackTrace();
            addErrorMessage("Couldn't remove cheese " + cheese);
            return ERROR;
        }
    }
}
