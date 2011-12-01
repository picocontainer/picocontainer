/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by various                           *
 *****************************************************************************/
package org.picocontainer.script.testmodel;

/**
 * @author Stephen Molitor
 */
public final class HasParams {

    private final String params;

    public HasParams(String a, String b, String c) {
        params = a + b + c;
    }

    public String getParams() {
        return params;
    }
}
