/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *****************************************************************************/

package com.picocontainer.persistence.hibernate.annotations;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.picocontainer.persistence.hibernate.Pojo;

/**
 * Just a pojo to make hibernate happy.
 * 
 * @author Konstantin Pribluda
 * @author Michael Rimov
 */
@Entity
@SuppressWarnings("serial")
public class AnnotatedPojo extends Pojo implements Serializable {


    @Id
    @GeneratedValue
    private Integer id;
    

    @Column(name="Foo", length=34)
    private String foo;

    public AnnotatedPojo() {
        super();
    }

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
