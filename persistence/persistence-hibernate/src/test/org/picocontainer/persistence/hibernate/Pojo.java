/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package org.picocontainer.persistence.hibernate;

/**
 * Just a pojo to make hibernate happy.
 * 
 * @author Konstantin Pribluda
 */
public class Pojo implements Fooable {

    private Integer id;
    private String foo;

    public Pojo() {}

    public Integer getId() {
        return id;
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

}
