/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.script.testmodel;

public final class ThingThatTakesParamsInConstructor {
    private final String value;
    private final Integer intValue;

    public ThingThatTakesParamsInConstructor(String value, Integer intValue) {
        this.value = value;
        this.intValue = intValue;
    }

    public String getValue() {
        return value + intValue;
    }
}
