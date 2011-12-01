/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Idea by Rachel Davies, Original code by Aslak Hellesoy and Paul Hammant   *
 *****************************************************************************/

package org.picocontainer.script.testmodel;

import junit.framework.Assert;

public final class FredImpl {
    final Wilma wilma;

    public FredImpl(Wilma wilma) {
        this.wilma = wilma;
        Assert.assertNotNull("Wilma cannot be passed in as null", wilma);
        wilma.hello();
    }

    public Wilma wilma() {
        return wilma;
    }
}
