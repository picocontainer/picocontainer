/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package com.picocontainer.testmodel;

import java.io.Serializable;

import junit.framework.Assert;

/**
 * @author steve.freeman@m3p.co.uk
 */
@SuppressWarnings("serial")
public class DependsOnTouchable implements Serializable {
    public final Touchable touchable;

    public DependsOnTouchable(final Touchable touchable) {
        Assert.assertNotNull("Touchable cannot be passed in as null", touchable);
        touchable.touch();
        this.touchable = touchable;
    }

    public Touchable getTouchable() {
        return touchable;
    }
}
