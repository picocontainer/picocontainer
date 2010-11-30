/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Aslak Hellesoy and Paul Hammant                          *
 *****************************************************************************/
package org.picocontainer.script.xml;


import java.lang.reflect.Type;

import org.picocontainer.PicoContainer;
import org.picocontainer.adapters.AbstractAdapter;

/**
 * component adapter to test script instantiation.
 */
@SuppressWarnings({ "serial", "unchecked" })
public final class TestAdapter extends AbstractAdapter {

    final String foo;
    final String blurge;
    final int bar;

    public TestAdapter(String foo, int bar, String blurge) {
        super(TestAdapter.class, TestAdapter.class);
        this.foo = foo;
        this.bar = bar;
        this.blurge = blurge;
    }

    public void verify(PicoContainer pico) {
    }

    public Object getComponentInstance(PicoContainer pico, Type into) {
        return null;
    }

    public String getDescriptor() {
        return null;
    }
}




