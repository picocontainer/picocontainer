/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.testmodel;

/**
 *
 * @author greg
 */
public final class CoupleBean {
    private final PersonBean personA;
    private final PersonBean personB;

    public CoupleBean(PersonBean a, PersonBean b) {
        this.personA = a;
        this.personB = b;
    }

    public PersonBean getPersonA() {
        return personA;
    }

    public PersonBean getPersonB() {
        return personB;
    }
}
