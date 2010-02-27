/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.picocontainer.script.testmodel;

import java.io.Serializable;

/**
 * @author Stephen Molitor
 */
public class IdentifiableMixin implements Identifiable, AnotherInterface {

    private Serializable id;

    public IdentifiableMixin(IdGenerator generator) {
        this.id = generator.nextId();
    }

    public IdentifiableMixin() {
        this(new IdGeneratorImpl());
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

}