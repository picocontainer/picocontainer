/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package com.picocontainer.web.sample.struts2.pwr;

import java.io.Serializable;

/**
 * @author Stephen Molitor
 * @author Mauro Talevi
 */
@SuppressWarnings("serial")
public class Cheese implements Serializable {

    private String name;
    private String country;

    public Cheese() {
        // default constructor used by some frameworks
    }

    public Cheese(final String name, final String country) {
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

    public void setName(final String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    @Override
	public String toString() {
        return "[Cheese name=" + name + ", country=" + country + "]";
    }

}