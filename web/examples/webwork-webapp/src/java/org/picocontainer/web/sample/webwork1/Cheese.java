/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.web.sample.webwork1;

import java.io.Serializable;

/**
 * @author Stephen Molitor
 * @author Mauro Talevi
 */
public class Cheese implements Serializable {

    private String name;
    private String country;

    public Cheese() {
        // default constructor used by some frameworks
    }

    public Cheese(String name, String country) {
        this.name = name;
        this.country = country;
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (country == null) {
            throw new NullPointerException("country");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String toString() {
        return "[Cheese name=" + name + ", country=" + country + "]";
    }

}