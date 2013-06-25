/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           * 
 * Original code by Centerline Computers, Inc.                               *
 *****************************************************************************/
package com.picocontainer.web.samples.jsf;

import java.io.Serializable;

/**
 * Add Cheese Controller.
 * @author Michael Rimov
 */
public class AddCheeseController implements Serializable {


    /**
     * Name of the cheese to add.
     */
    private String name;
    
    
    /**
     * Country of the cheese to add.
     */
    private String country;

    
    /**
     * CDI injected Cheese Service.
     */
    private final CheeseService service;
    
    
    /**
     * Constructor for the Cheese Controller that links it
     * with the cheese service.
     * @param service
     */
    public AddCheeseController(CheeseService service) {
       this.service = service;
    }
    
    
    /**
     * Adds a cheese via the CDI injected cheese service.
     * @return the next action to use when considering JSF
     * mapping.
     */
    public String addCheese() {
        Cheese cheese = new Cheese(name,country);
        service.save(cheese);
        return "addCheeseSuccess";        
    }


    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }


    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    
  
    
}
