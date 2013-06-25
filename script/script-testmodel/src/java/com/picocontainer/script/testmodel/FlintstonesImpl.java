/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package com.picocontainer.script.testmodel;

import junit.framework.Assert;

public class FlintstonesImpl {
    public FlintstonesImpl(Wilma wilma, FredImpl fred) {
        Assert.assertNotNull("Wilma cannot be passed in as null", wilma);
        Assert.assertNotNull("FredImpl cannot be passed in as null", fred);
    }
}
